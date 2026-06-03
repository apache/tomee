# Apache TomEE — Security Model

Apache TomEE is a Jakarta EE and MicroProfile runtime. The Apache TomEE Security Team reviews reported vulnerabilities against the security model described below. Reports that fall outside it will be rejected.

Report suspected vulnerabilities privately to **security@apache.org**. Do not file security reports as public Jira tickets or GitHub issues.

## Triage dispositions

Every report resolves to exactly one of four dispositions. A triager applies the disposition and cites the section below that licenses it.

- **VALID** — a vulnerability in TomEE's own attack surface (see *In scope* below). Accepted and fixed.
- **BY-DESIGN** — the behaviour is an intentional property of a protocol or feature, not a defect (e.g. EJBd Java-object deserialization; see *Connectors and transports*).
- **OUT-OF-MODEL** — a real or hypothetical issue that falls outside the trust boundaries of this model. Cite the controlling section: a trusted actor (*Administrative users*, *Deployed applications*), the trusted-network deployment contract (*Connectors and transports*, *Embedded network services*, *Service discovery*), a flaw in a bundled library as released (*Bundled third-party libraries*), or generic resource exhaustion / DoS (*Connectors and transports*).
- **KNOWN-NON-FINDING** — the report matches an entry in *Known non-findings*. Pre-adjudicated; repeated submissions are treated as spam. This label takes precedence: if a report matches the Known non-findings list, label it here rather than by its underlying reason.

### In scope — what IS a TomEE vulnerability

These are the active search targets. A finding in any of them is VALID:

- **Bundled-webapp endpoints** — the TomEE web applications shipped in ASF distributions (the TomEE webapp, web console, REST admin endpoints, `webaccess`, plus/plume admin UIs), including CSRF that tricks an administrator (see *Administrative users*, *Deployed applications*).
- **TomEE integration code** — the wiring that exposes bundled libraries through TomEE's deployment model, configuration, and defaults (see *Bundled third-party libraries*).
- **TomEE-modified library code** — anything TomEE forks, patches, shades/relocates, or bytecode-transforms at build time, even when it sits in an upstream package namespace (see *Bundled third-party libraries*).
- **Amplification resource exhaustion** — a single bounded, well-formed request that causes disproportionate (super-linear or unbounded) resource consumption because of a defect in TomEE-owned code, on a connector that does not require a trusted network (see *Connectors and transports*).

## Administrative users

Administrative users are always considered to be trusted. Reports for vulnerabilities where an attacker already has access to or control over any of the following will be rejected:

- TomEE binaries and scripts (everything under a TomEE distribution's `bin/` and `lib/`).
- TomEE configuration (`conf/`, `conf/tomee.xml`, `conf/system.properties`, `conf/server.xml`, `conf/conf.d/`, realm files, JAAS login configuration).
- Log files (`logs/`).
- The temp directory (by default `$CATALINA_BASE/temp`).
- Application working directories (by default `$CATALINA_BASE/work`).
- The TomEE-provided web applications shipped with the distribution (`tomee/`, `webaccess/`, plus/plume admin UIs).
- The JMX API (local or remote).
- The Java Attach API or any other debugging or agent interface.
- The OpenEJB `apps/` and `webapps/` deployment directories.
- Resource definitions in `tomee.xml` / `resources.xml` (datasources, connectors, services).

Reports for vulnerabilities where an attacker tricks an administrative user into performing an action the administrator did not intend (e.g. CSRF in the bundled TomEE web applications) will be accepted.

## Deployed applications

Applications and modules deployed to TomEE — WARs, EARs, EJB-JARs, RARs — are considered to be trusted. This applies whether they are dropped into `webapps/` or `apps/`, deployed via the Deployer, installed by `tomee-maven-plugin`, or embedded via `tomee-embedded`. It also covers:

- Application-provided JAX-RS / JAX-WS endpoints (handled by CXF but configured by the application).
- Application-provided JMS clients and listeners.
- Application-provided JPA entities and persistence units.
- Application-provided CDI beans, interceptors, and decorators.

Vulnerabilities in application code are application vulnerabilities, not TomEE vulnerabilities.

Applications that enable functionality permitting modification of the deployed application (hot-deploy endpoints, WebDAV, HTTP `PUT`, custom upload handlers) are expected to secure that functionality themselves. Failure to do so is an application vulnerability, not a TomEE vulnerability.

Reports against the TomEE-provided web applications shipped with standard distributions from the ASF — the TomEE webapp, web console, REST admin endpoints, `webaccess`, plus/plume admin UIs — will be accepted. Reporters should review the *Security Considerations* notes in the documentation for the version under test before filing.

## Connectors and transports

Data received via any TomEE-exposed connector or transport is considered to be untrusted, regardless of protocol. This explicitly covers the OpenEJB transports under `server/`:

- `openejb-ejbd` — the EJBd binary protocol.
- `openejb-http` — OpenEJB's HTTP transport for remote EJB and REST invocations.
- `openejb-hessian` — Hessian RPC transport.
- `openejb-ssh` — SSH-based admin transport.
- CXF-backed JAX-WS and JAX-RS endpoints exposed by TomEE itself (separate from application-provided endpoints in the *Deployed applications* section).

**EJBd Java-object deserialization.** The EJBd protocol deserializes Java objects from clients by design — this is what the protocol is. Exposing EJBd on an untrusted network without authentication and a deserialization filter is misuse, not a TomEE vulnerability. An optional transport flag enables TLS, but the default cipher suite is not authenticated; operators are responsible for selecting authenticated cipher suites and configuring a JEP 290 deserialization filter (`-Djdk.serialFilter=...`) when the transport is reachable from untrusted clients.

All clients — including reverse proxies and remote EJB clients using `openejb-client` — are responsible for the consequences of the data they present to TomEE. If a client presents a malformed request that TomEE processes per the protocol specification, any security impact to the client is the client's responsibility.

**Resource exhaustion and denial of service.** Generic DoS is out of scope: request or connection floods, slow-client (Slowloris-style) attacks, large-but-linear payloads, and resource cost inherent to a protocol or to a bundled library as released are not treated as TomEE vulnerabilities. The exception is amplification — a single bounded, well-formed request that triggers disproportionate (super-linear or unbounded) CPU, memory, or thread consumption because of a defect in TomEE-owned code (for example a decompression bomb, entity expansion, or quadratic parsing in TomEE's integration or patched code), on a connector that does not require a trusted network. Such amplification defects are in scope. The trusted-network transports (EJBd, the SSH admin transport, the embedded brokers and databases, JMX, and discovery) are excluded, because exposing them to untrusted clients at all is already misuse.

## Embedded network services

TomEE can be configured to start network-listening services beyond the main HTTP and EJBd transports. Each is admin-enabled; when enabled, the following services require a trusted network or explicit authentication, and exposing them on an untrusted network without hardening is misuse, not a TomEE vulnerability:

- **Embedded databases with network listeners.** Derby Network Server (`openejb-derbynet`) and HSQLDB Server (`openejb-hsql`) listen on TCP ports when configured to do so. Operators are responsible for binding them to trusted interfaces or enabling their authentication.
- **Embedded ActiveMQ broker.** When `openejb-activemq` is configured with a network connector (e.g. a `tcp://` transport), the broker accepts JMS client connections. Operators are responsible for the broker's authentication, authorization, and TLS configuration.
- **JMX over RMI.** TomEE exposes JMX MBeans. When a remote JMX connector is enabled, operators are responsible for its authentication and TLS configuration. (See also *Administrative users*: an attacker who already has JMX access is treated as administrative.)
- **The SSH admin transport** (`openejb-ssh`). Already covered as a connector in *Connectors and transports*; mentioned here for completeness because it is also an admin control plane.

Reports against any of these services that assume they should be safe to expose on an untrusted network without authentication will be rejected.

## Service discovery

OpenEJB provides peer-to-peer service discovery between cluster nodes via two mechanisms:

- **Multipoint discovery** (`MultipointDiscoveryAgent` / `MultipointServer` in `openejb-server`). TCP-based gossip between known peers.
- **Multicast discovery** (`MulticastDiscoveryAgent`, `MulticastPulseAgent` in `openejb-multicast`). UDP multicast for peer discovery on a local network.

A trusted network is integral to this feature. Operators who need to run discovery across an untrusted network are responsible for providing transport-level protection (VPN, network segmentation, etc.) themselves.

Reports against multipoint or multicast discovery that depend on the absence of network-layer protection — including deserialization of cluster traffic, peer impersonation, or message injection — will be rejected on the basis that a trusted network is part of the deployment contract.

## Bundled third-party libraries

TomEE distributions bundle implementations of Jakarta EE and MicroProfile specifications from upstream projects, including (non-exhaustive):

- Apache CXF (JAX-RS, JAX-WS).
- Apache ActiveMQ (JMS broker and client).
- Apache MyFaces and Mojarra (Jakarta Faces).
- Apache OpenJPA (Jakarta Persistence).
- Apache BVal (Jakarta Validation).
- HSQLDB and Apache Derby (embedded databases).
- MicroProfile implementations (JWT, Config, etc.).

TomEE owns only its **integration code** — the wiring that exposes these libraries through TomEE's deployment model, configuration, and defaults (for example, how `openejb-cxf-rs` integrates CXF with the EJB container, or how a `<Resource>` in `tomee.xml` configures an ActiveMQ broker).

Reports of vulnerabilities in TomEE's integration code are accepted. Reports of vulnerabilities in the upstream library itself — independent of how TomEE wires it in — are out of scope; they should be reported to the upstream project's security contact.

**TomEE-modified library code is in scope.** TomEE does not ship all bundled libraries unmodified. Where TomEE forks an upstream class (e.g. the BCEL `ConstantPool`/`ConstantPoolGen` fork under `openejb.shade.*`, the patched Tomcat `ServerInfo`), plants a class in an upstream package to reach internals (e.g. `org.apache.bval.jsr.job.ConstraintValidators`, `org.apache.catalina.startup.OpenEJBContextConfig`), shades or relocates a library under TomEE coordinates (e.g. `commons-dbcp2-shade`, `taglibs-shade`), or transforms a bundled jar's bytecode at build time via `tomee-patch-plugin` (including the `javax`→`jakarta` migration), the resulting code is **TomEE's**. A vulnerability introduced or altered by a TomEE patch, fork, shade, or bytecode transform is a TomEE vulnerability and is in scope — even when the affected class sits in an upstream package namespace. Out of scope is limited to flaws present in the upstream artifact as released, unchanged by TomEE.

If you are unsure whether a finding is in TomEE's integration code or in the upstream library, send it to **security@apache.org** and the team will route it.

## Logging

Security-sensitive information will not be logged with the default configuration apart from anything included in the request URI.

Security-sensitive information may be logged with modified logging configurations, particularly when debug logging is enabled. Common examples in TomEE deployments:

- Raising OpenEJB log levels.
- Enabling CXF request/response logging (JAX-RS / JAX-WS).
- Turning on JPA SQL logging (e.g. OpenJPA `openjpa.Log`, Hibernate `show_sql`).
- Enabling JMS broker trace logging.

The default logs are likely to contain personally identifiable information (PII) such as the IP address of users.

TomEE is not responsible for the content of log messages generated by deployed applications.

## Known non-findings

The following non-findings are frequently reported despite being invalid under the model above. Repeated reports of non-findings from any source will be treated as spam and will result in all email from the source being blocked at the ASF's border.

1. Any report that depends on deserialisation of OpenEJB multipoint or multicast discovery traffic — discovery requires a trusted network (see *Service discovery*).
2. Any report that depends on write access to an application's `docBase`, to the OpenEJB `apps/` deployment directory, or to any other location already controlled by an administrative user per the *Administrative users* section above.
3. Any report that depends on deploying a malicious application — deployed applications are trusted (see *Deployed applications*).
4. Any report against the EJBd protocol, JMX, the embedded ActiveMQ broker, Derby Network Server, HSQLDB Server, or other management or admin endpoints that assumes they should be safe to expose on an untrusted network without authentication (see *Connectors and transports* and *Embedded network services*).
5. Any report against bundled third-party libraries — including but not limited to CXF, ActiveMQ, MyFaces, Mojarra, OpenJPA, BVal, HSQLDB, Derby, and MicroProfile implementations — where the root cause is in the upstream library as released rather than in TomEE's integration code or in any code TomEE forks, patches, shades, or byte-code-transforms (see *Bundled third-party libraries*).
6. Any report of generic denial of service — request or connection floods, slow-client attacks, large-but-linear payloads, or resource cost inherent to a protocol or bundled library — absent a specific amplification defect in TomEE-owned code (see *Resource exhaustion and denial of service* under *Connectors and transports*).
