---
name: triage-dependabot-prs
disable-model-invocation: true
description: Triage open Dependabot PRs on apache/tomee - close, file TOMEE Jira issues, squash merge. Use when the user asks about dependabot or dependency bump PRs.
---

# TomEE Dependabot

Run the phases in order. Phases 2 and 3 end at a **gate**: show the user the result and wait for an explicit go-ahead. All writes to GitHub or Jira (close, rename, merge, create, edit, transition) happen in phase 4.

Run from a TomEE checkout. `origin` means the remote for `apache/tomee` (substitute its name if it differs); fetch it first. Pass `-R apache/tomee` to every `gh` call.

## Jira access

**Reads** use the public REST API, no login needed:

```sh
curl -s https://issues.apache.org/jira/rest/api/2/project/TOMEE/versions
curl -s -G https://issues.apache.org/jira/rest/api/2/search \
  --data-urlencode 'jql=project = TOMEE AND summary ~ "Tomcat" ORDER BY created DESC' \
  --data-urlencode 'fields=summary,issuetype,fixVersions,status' --data-urlencode maxResults=20
```

**Writes** go through a Jira MCP server (checked in phase 1). The user picks its name, so find it by its tools (issue create, edit, transition, current-user profile), not by server or tool name. If none is configured, offer to help set up [mcp-atlassian](https://github.com/sooperset/mcp-atlassian) against ASF Jira (`https://issues.apache.org/jira`, Server/Data Center), following its current docs. The user enters credentials into their config themselves, never into the chat. Without a working server, phase 4 does the GitHub side and lists the Jira writes for the user to do by hand.

## Jakarta EE line guardrail

Each branch ships one Jakarta EE version, and each EE implementation has a release line bound to it. A bump onto another line compiles and passes CI but ships a component built for the wrong spec. **Merge only bumps that stay on the branch's line**; close the rest with a comment naming the line, whatever CI says.

The project version in `git show origin/<branch>:pom.xml` gives the EE version (TomEE 11 = EE 11, TomEE 10 = EE 10). Lines as of TomEE 11.0.0 / 10.3.0:

| Component | TomEE 11 (main) | TomEE 10 (tomee-10.x) |
|---|---|---|
| Tomcat | 11.0.x | 10.1.x |
| MyFaces | 4.1.x | 4.0.x |
| Mojarra (`org.glassfish:jakarta.faces`) | 4.1.x | 4.0.x |
| ActiveMQ | 6.3.x | 6.3.x |

ActiveMQ is not bound to the EE version: 6.2 and 6.3 both build against Jakarta Messaging 3.1 on Java 17, and the modules TomEE ships (broker, client, jdbc-store, openwire-legacy, ra) need only `jakarta.jms-api`, `jakarta.annotation-api` and `jakarta.resource-api`, which TomEE provides at its own EE level. 6.3's EE 11 dependencies come only from the web console and HTTP transport, which TomEE does not ship. tomee-10.x is on 6.3 because CVE-2026-74761 has no 6.2.x fix (TOMEE-4721). For a new ActiveMQ minor on either branch, compare `jakarta-jms-api-version` and `javaVersion` in the new `activemq-parent` pom, and the non-test dependencies of those five modules, with the branch's platform.

For a component not in the table, read its spec version off its release notes or project page (Tomcat: https://tomcat.apache.org/whichversion.html) and compare it with what the branch's platform lists.

The ignore list in `.github/dependabot.yml` on `main` (it configures both branches) covers these groups, but is only a first filter; the guardrail is the check.

**CXF-aligned libraries** (santuario, woodstox, opensaml): close; the CXF upgrade brings them. **Spec APIs and MicroProfile implementations** get a spec review.

### Spec review

Verdict per PR: **same spec** (handle like any other PR) or **spec moves** (close; the comment names old and new spec version).

**Jakarta spec APIs** (`jakarta.*:*`): the major of `version.jakartaee-api` in the branch's root `pom.xml` is the EE version. Look up the component spec version that platform lists at `https://jakarta.ee/specifications/platform/<N>/` (the `jakarta-ee-spec-reader` skill does this). Same spec only for a service release of that spec version (`jakarta.faces-api` 4.1.2 → 4.1.3 on EE 11); a different major or minor is a spec move.

**MicroProfile spec APIs** (`org.eclipse.microprofile*`): the root `pom.xml` pins each spec in `version.microprofile.<spec>`. Same spec only for a patch of that exact version (`4.0.1` → `4.0.2`).

**MicroProfile implementations** (`io.smallrye*`, micrometer, opentelemetry, opentracing), pinned in `version.microprofile.impl.<spec>`:

1. Read the branch's spec version, `version.microprofile.<spec>` on `origin/<branch>`.
2. Read the spec version the *new* implementation targets from its parent pom on Maven Central (`io/smallrye/<project>/smallrye-<project>-parent`):

   ```sh
   curl -s https://repo.maven.apache.org/maven2/io/smallrye/config/smallrye-config-parent/$V/smallrye-config-parent-$V.pom \
     | grep -iE 'microprofile[^<]*>[0-9]'
   ```

   If the parent has no such property, use the `org.eclipse.microprofile.*` dependency version in the implementation's own pom.
3. Equal major.minor is same spec, anything else a spec move. SmallRye OpenTelemetry 2.7.0 → 2.15.1 moves MP Telemetry 1.1 → 2.1 (spec move on a branch pinned to 1.1); SmallRye Config 3.18.2 → 4.0.0 stays on MP Config 3.1 (same spec).
4. For a new implementation major, check the Java baseline (`maven.compiler.release` in the root `pom.xml`). Byte 8 of a class file is 61 for Java 17, 65 for Java 21; above the baseline is a spec move.

   ```sh
   curl -s -o impl.jar <jar url> && unzip -p impl.jar <some .class> | head -c 8 | od -An -tu1
   ```

A same-spec new major can still break TomEE's integration code, which CI only compiles: list it at the gate as a merge candidate flagged with the new major, for the user to decide.

## 1. Collect

```sh
gh pr list -R apache/tomee --author app/dependabot --state open --limit 200 \
  --json number,baseRefName,title,files,statusCheckRollup
```

From the versions endpoint, take each branch's unreleased fix version: `released: false` and matching the branch's project version (e.g. `11.0.0` for main, `10.3.0` for tomee-10.x; not `JakartaEE API 11.0.0`).

Look for a Jira MCP server among the available tools and call its current-user profile tool to confirm a login to `issues.apache.org`; if missing, offer the setup above now so it doesn't block phase 4.

Done when every open PR has its branch, changed files and CI state, each branch has its fix version, and Jira write access is known.

## 2. Classify

Put every PR in exactly one class:

- **Build**: Maven plugins, plugin tooling such as `maven-plugin-annotations`, the `org.apache:apache` parent pom.
- **Shipped**: the artifact appears in a BOM of that branch, so it lands in a distribution.
- **Misc**: everything else (test-only libraries, TCK modules, examples, `utils/` integrations).

```sh
git grep -c "<artifactId>$ARTIFACT</artifactId>" origin/$BRANCH -- 'boms/*/pom.xml'
```

For a PR that bumps a version *property* (`version.activemq`, `tomcat.version`), trace the property to its artifacts in the root `pom.xml` first.

Flag on top of the class:

- guardrail breaks, and the spec review verdict for every spec API and MicroProfile implementation PR;
- pre-releases and new majors (`-beta`, `-M`, `-rc`, `X.0.0`), with their prerequisites checked (the core Maven plugins' 4.x line needs Maven 4; the build runs Maven 3, so it fails the first `install`);
- a pom comment pinning the old version on purpose (e.g. the rest-client TCK pins old Jetty);
- a failing check: read `gh run view <id> --log-failed -R apache/tomee` and report the actual error.

CI runs `clean install -DskipTests -Pstyle,rat`: green means it compiles and passes style and license checks, not tests or TCKs. Say so when a bump's risk is runtime behaviour.

**Gate:** tables per class (PR, branch, change, flags).

## 3. Plan Jira

Only shipped dependencies with precedent get an issue: search for earlier issues on each (`project = TOMEE AND summary ~ "<name>" ORDER BY created DESC`). A dependency with past `Dependency upgrade` issues gets one; build and misc PRs never do, even with a stray precedent (Hibernate has one by accident).

One issue per dependency and target version: when both branches bump to the same version, one issue carries both fix versions. Look for an issue on the dependency in the branch's unreleased fix version:

- **Exists**: retitle it to the new version (add the other branch's fix version if needed); leave its assignee alone.
- **None**: create one. Type `Dependency upgrade`, summary `<Name> <version>` (e.g. `Tomcat 11.0.26`, `Apache ActiveMQ 6.2.10`, `MyFaces 4.0.4`), the unreleased fix version of each branch it covers, assigned to the user running the skill (from the MCP profile lookup in phase 1, or ask).

**Gate:** every PR with its disposition: close (with reason), merge with issue (retitled or new, with the planned summary), or merge without issue.

## 4. Execute

Every comment and issue body carries the agent disclosure from `AGENTS.md`.

1. Close: `gh pr close <PR> -R apache/tomee --comment "<reason: error, pinned version, or EE line>"`. Closing is the whole response; ignore-list changes are a separate decision for the user.
2. For each PR with an issue, in order:
   1. Create or retitle the issue (MCP).
   2. `gh pr edit <PR> -R apache/tomee --title "TOMEE-XXXX - <Dependabot title>"`
   3. `gh pr merge <PR> -R apache/tomee --squash --subject "TOMEE-XXXX - <Dependabot title> (#PR)"`
   4. Transition the issue to Resolved, resolution Fixed, unless it already is (MCP). Closed happens at release.
3. PRs without an issue: `gh pr merge <PR> -R apache/tomee --squash` (subject: Dependabot's title).

Each merge can make Dependabot rebase other PRs on that branch. Before every merge, confirm the PR is still mergeable with passing checks (`gh pr view <PR> -R apache/tomee --json mergeable,statusCheckRollup`); skip and report any that is not.

Done when every planned PR is closed or merged and every issue is Resolved. Report the result per PR, including anything skipped and why.
