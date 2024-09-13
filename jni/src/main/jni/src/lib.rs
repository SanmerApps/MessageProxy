use android_logger::Config;
use lettre::address::AddressError;
use lettre::message::header::ContentType;
use lettre::message::Mailbox;
use lettre::transport::smtp::authentication::Credentials;
use lettre::transport::smtp::client::{Tls, TlsParameters};
use lettre::transport::smtp::Error as SmtpError;
use lettre::{Address, Message, SmtpTransport, Transport};
use log::LevelFilter;
use std::fmt::{Display, Formatter};
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

macro_rules! jni_throw {
    ($($arg:tt)*) => {
        let err = JRuntimeException::new(format!($($arg)*));
        err.throw();
    };
}

macro_rules! or_throw {
    ($block:expr) => {
        match $block {
            Ok(v) => v,
            Err(e) => {
                jni_throw!("{e:?}");
                return;
            }
        }
    };
}

macro_rules! or_panic {
    ($block:expr, $msg:expr) => {
        match $block {
            Ok(v) => v,
            Err(_) => panic!($msg),
        }
    };
}

pub struct JRuntimeException {
    message: String,
}

define_type!(JRuntimeException, "java/lang/RuntimeException");

impl JRuntimeException {
    fn new<S: Into<String>>(message: S) -> JRuntimeException {
        JRuntimeException {
            message: message.into(),
        }
    }

    fn throw(self) {
        Context::with_attached(|ctx| unsafe {
            let class = or_panic!(
                ctx.find_class(c"java/lang/RuntimeException"),
                "BROKEN: find java/lang/RuntimeException failed"
            );
            let method = or_panic!(
                ctx.find_method(&class, c"<init>", c"(Ljava/lang/String;)V"),
                "BROKEN: find java/lang/RuntimeException.<init> failed"
            );
            let message = ctx.new_string(&self.message);
            let message = (&message).into();
            let throwable = or_panic!(
                ctx.new_object(&class, method, [message]),
                "BROKEN: create java/lang/RuntimeException failed"
            );
            ctx.throw(&throwable);
        })
    }
}

#[derive(Debug)]
pub struct JAddress {
    user: String,
    domain: String,
}

define_type!(JAddress, "dev/sanmer/email/Address");

impl From<Object<'_, JAddress>> for JAddress {
    fn from(value: Object<'_, JAddress>) -> Self {
        Context::with_attached(|ctx| Self {
            user: get_string!(ctx, value, "getUser"),
            domain: get_string!(ctx, value, "getDomain"),
        })
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

impl TryFrom<Object<'_, JMailbox>> for JMailbox {
    type Error = AddressError;

    fn try_from(value: Object<'_, JMailbox>) -> Result<Self, Self::Error> {
        Context::with_attached(|ctx| {
            let email: Object<JAddress> = get_object!(ctx, value, "getEmail");
            let email = JAddress::from(email);
            Ok(Self {
                name: get_string!(ctx, value, "getName"),
                email: email.try_into()?,
            })
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

impl TryFrom<Object<'_, JMessage>> for JMessage {
    type Error = AddressError;

    fn try_from(value: Object<'_, JMessage>) -> Result<Self, Self::Error> {
        Context::with_attached(|ctx| {
            let from: Object<JMailbox> = get_object!(ctx, value, "getFrom");
            let from = JMailbox::try_from(from)?;
            let to: Object<JMailbox> = get_object!(ctx, value, "getTo");
            let to = JMailbox::try_from(to)?;
            Ok(Self {
                from: from.into(),
                to: to.into(),
                subject: get_string!(ctx, value, "getSubject"),
                body: get_string!(ctx, value, "getBody"),
            })
        })
    }
}

impl Display for JMessage {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(
            f,
            "from({:?} <{}>), to({:?} <{}>)",
            self.from.name, self.from.email, self.to.name, self.to.email
        )
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

impl From<Object<'_, JConfig>> for JConfig {
    fn from(value: Object<'_, JConfig>) -> Self {
        Context::with_attached(|ctx| Self {
            server: get_string!(ctx, value, "getServer"),
            port: get!(ctx, value, "getPort"),
            username: get_string!(ctx, value, "getUsername"),
            password: get_string!(ctx, value, "getPassword"),
        })
    }
}

impl Display for JConfig {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}:{}", self.server, self.port)
    }
}

impl JConfig {
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

    fn send(self, message: &Message) -> Result<String, SmtpError> {
        self.transport.send(message).map(|r| {
            let message: Vec<&str> = r.message().collect();
            message.join(", ")
        })
    }
}

#[no_mangle]
pub extern "C" fn Java_dev_sanmer_email_Lettre_send<'ctx>(
    _ctx: &'ctx Context,
    _class: Class<'ctx, JLettre>,
    config: Object<'ctx, JConfig>,
    message: Object<'ctx, JMessage>,
) {
    let config = JConfig::from(config);
    log::debug!("Server: {config}");
    let message = or_throw!(JMessage::try_from(message));
    log::debug!("Message: {message}");
    let message = or_throw!(message.try_into());
    let lettre = or_throw!(JLettre::build(&config));
    let server_msg = or_throw!(lettre.send(&message));
    log::debug!("Server: {server_msg}")
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
