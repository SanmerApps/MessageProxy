use android_logger::Config;
use lettre::address::AddressError;
use lettre::message::header::ContentType;
use lettre::message::Mailbox;
use lettre::transport::smtp::authentication::Credentials;
use lettre::transport::smtp::client::{Tls, TlsParameters};
use lettre::transport::smtp::Error as SmtpError;
use lettre::{Address, Message, SmtpTransport, Transport};
use log::LevelFilter;
use typed_jni::sys::{jint, JavaVM, JNI_VERSION_1_6};
use typed_jni::{Class, Context, JString, Object, ObjectType, Signature, Type};

macro_rules! define_type {
    ($target:ident, $sig:expr) => {
        impl Type for $target {
            const SIGNATURE: Signature = Signature::Object($sig);
        }

        impl ObjectType for $target {}
    };
}

macro_rules! get_object {
    ($ctx:expr, $obj:expr, $name:expr) => {
        Option::unwrap($obj.call_method($ctx, $name, ()).unwrap())
    };
}

macro_rules! get_string {
    ($ctx:expr, $obj:expr, $name:expr) => {{
        let obj: Object<JString> = get_object!($ctx, $obj, $name);
        obj.get_string($ctx)
    }};
}

macro_rules! get {
    ($ctx:expr, $obj:expr, $name:expr) => {
        $obj.call_method($ctx, $name, ()).unwrap()
    };
}

macro_rules! catch {
    ($block:expr) => {
        match $block {
            Ok(v) => Some(v),
            Err(e) => {
                log::error!("{:?}", e);
                None
            }
        }
    };
}

macro_rules! skip {
    ($block:expr) => {{
        let Some(value) = catch!($block) else {
            return;
        };
        value
    }};
}

#[derive(Debug)]
pub struct JAddress {
    user: String,
    domain: String,
}

define_type!(JAddress, "dev/sanmer/email/Address");

impl JAddress {
    fn from_java<'ctx>(ctx: &'ctx Context, address: Object<'ctx, Self>) -> Self {
        Self {
            user: get_string!(ctx, address, "getUser"),
            domain: get_string!(ctx, address, "getDomain"),
        }
    }
}

impl TryFrom<JAddress> for Address {
    type Error = AddressError;

    fn try_from(value: JAddress) -> Result<Self, Self::Error> {
        Address::new(value.user, value.domain)
    }
}

#[derive(Debug)]
pub struct JMailbox {
    name: String,
    email: Address,
}

define_type!(JMailbox, "dev/sanmer/email/Mailbox");

impl JMailbox {
    fn from_java<'ctx>(
        ctx: &'ctx Context,
        mailbox: Object<'ctx, Self>,
    ) -> Result<Self, AddressError> {
        let email: Object<JAddress> = get_object!(ctx, mailbox, "getEmail");
        let email = JAddress::from_java(ctx, email);
        Ok(Self {
            name: get_string!(ctx, mailbox, "getName"),
            email: email.try_into()?,
        })
    }
}

impl From<JMailbox> for Mailbox {
    fn from(value: JMailbox) -> Self {
        if value.name.is_empty() {
            Mailbox::new(None, value.email)
        } else {
            Mailbox::new(Some(value.name), value.email)
        }
    }
}

#[derive(Debug)]
pub struct JMessage {
    from: Mailbox,
    to: Mailbox,
    subject: String,
    body: String,
}

define_type!(JMessage, "dev/sanmer/email/Message");

impl JMessage {
    fn from_java<'ctx>(
        ctx: &'ctx Context,
        message: Object<'ctx, Self>,
    ) -> Result<Self, AddressError> {
        let from: Object<JMailbox> = get_object!(ctx, message, "getFrom");
        let from = JMailbox::from_java(ctx, from)?;
        let to: Object<JMailbox> = get_object!(ctx, message, "getTo");
        let to = JMailbox::from_java(ctx, to)?;
        Ok(Self {
            from: from.into(),
            to: to.into(),
            subject: get_string!(ctx, message, "getSubject"),
            body: get_string!(ctx, message, "getBody"),
        })
    }
}

impl TryFrom<JMessage> for Message {
    type Error = lettre::error::Error;

    fn try_from(value: JMessage) -> Result<Self, Self::Error> {
        Message::builder()
            .from(value.from)
            .to(value.to)
            .subject(value.subject)
            .header(ContentType::TEXT_PLAIN)
            .body(value.body)
    }
}

#[derive(Debug)]
pub struct JConfig {
    server: String,
    port: i32,
    username: String,
    password: String,
}

define_type!(JConfig, "dev/sanmer/email/Lettre$Config");

impl JConfig {
    fn from_java<'ctx>(ctx: &'ctx Context, config: Object<'ctx, Self>) -> Self {
        Self {
            server: get_string!(ctx, config, "getServer"),
            port: get!(ctx, config, "getPort"),
            username: get_string!(ctx, config, "getUsername"),
            password: get_string!(ctx, config, "getPassword"),
        }
    }

    fn port(&self) -> u16 {
        u16::try_from(self.port).unwrap_or(465)
    }
}

pub struct JLettre {
    transport: SmtpTransport,
}

define_type!(JLettre, "dev/sanmer/email/Lettre");

impl JLettre {
    fn build(config: &JConfig) -> Result<Self, SmtpError> {
        let tls = TlsParameters::new(config.server.to_owned())?;
        let creds = Credentials::new(config.username.to_owned(), config.password.to_owned());
        let transport = SmtpTransport::builder_dangerous(&config.server)
            .port(config.port())
            .tls(Tls::Wrapper(tls))
            .credentials(creds)
            .build();

        Ok(Self { transport })
    }

    fn send(self, message: &Message) -> Result<(), SmtpError> {
        self.transport.send(message).map(|_| ())
    }
}

#[no_mangle]
pub extern "C" fn Java_dev_sanmer_email_Lettre_send<'ctx>(
    ctx: &'ctx Context,
    _class: Class<'ctx, JLettre>,
    config: Object<'ctx, JConfig>,
    message: Object<'ctx, JMessage>,
) {
    let config = JConfig::from_java(ctx, config);
    let message = skip!(JMessage::from_java(ctx, message));
    log::debug!("{:?}", message);
    let message = skip!(message.try_into());
    let lettre = skip!(JLettre::build(&config));
    skip!(lettre.send(&message));
}

#[no_mangle]
pub extern "C" fn JNI_OnLoad(vm: *mut JavaVM, _: *const ()) -> jint {
    typed_jni::attach_vm(vm);
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Debug)
            .with_tag("Lettre"),
    );
    JNI_VERSION_1_6 as _
}
