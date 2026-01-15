# WE ARE NO LONGER ACCEPTING CHANGES TO THIS REPOSITORY AND WILL BE DISCONTINUING SUPPORT FOR THIS PROJECT END OF JAN 2026
Please see alternatives such as a https://github.com/hub4j/github-api

![release pipeline](https://github.com/spotify/github-java-client/actions/workflows/release.yml/badge.svg)
[![codecov](https://codecov.io/gh/spotify/github-java-client/branch/master/graph/badge.svg?token=ADHNCIESSL)](https://codecov.io/gh/spotify/github-java-client)[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![lifecycle: beta](https://img.shields.io/badge/lifecycle-beta-509bf5.svg)
[![Maven Central](https://img.shields.io/maven-central/v/com.spotify/github-client)](https://mvnrepository.com/artifact/com.spotify/github-client)

# github-java-client

A small Java library for talking to GitHub/GitHub Enterprise and interacting with projects.

It supports authentication via simple access tokens, JWT endpoints and GitHub Apps (via private key).

It is also very light on GitHub, doing as few requests as necessary.

This library is maintained by @spotify/gjc-maintainers. If you have any questions, issues or need a
review, please tag this team in the relevant PR/issue.

## Getting Started

You can find this library in [maven central repository](https://mvnrepository.com/artifact/com.spotify/github-client).

Include the latest version of github-client into your project:

In Maven:

```xml

<dependency>
    <groupId>com.spotify</groupId>
    <artifactId>github-client</artifactId>
    <version>version</version>
</dependency>
```

## Authenticating

### Simple access token

```java
final GitHubClient githubClient = GitHubClient.create(URI.create("https://api.github.com/"), "my-access-token");
```

### Private key

To authenticate as a GitHub App, you must provide a private key and the App ID, together with the API URL.

```java
final GitHubClient githubClient =
        GitHubClient.create(
                URI.create("https://api.github.com/"),
                new File("/path-to-the/private-key.pem"),
                APP_ID);
```

Then, you can scope the client for a specific Installation ID, to do the operations at the installation level.
The client will manage the generation of JWT tokens, as well as requesting and caching the installation tokens
from GitHub.

```java
final GitHubClient scopedClient = GitHubClient.scopeForInstallationId(githubClient, INSTALLATION_ID);
```

It is also possible to provide the installation to the root client.

Refer
to [GitHub App Authentication Guide](https://developer.github.com/apps/building-github-apps/authenticating-with-github-apps/)
for more information.

## Usage

This library attempts to mirror the structure of GitHub API endpoints. As an example, to get details of a Commit, there
is
the `GET /repos/:owner/:repo/commits` API call, under the `repos` API. Therefore, the `getCommit` method lives in the
RepositoryClient.

```java
final RepositoryClient repositoryClient = githubClient.createRepositoryClient("my-org", "my-repo");
log.info(repositoryClient.getCommit("sha").get().htmlUrl());
```

Another example of the mirrored structure is that some of the APIs are nested under a parent API.
For example, endpoints related to check runs or issues are nested under the Repository client:

```java
final ChecksClient checksClient = repositoryClient.createChecksApiClient();
checksClient.createCheckRun(CHECK_RUN_REQUEST);

final IssueClient issueClient = repositoryClient.createIssueClient();
issueClient
        .createComment(ISSUE_ID, "comment body")
        .thenAccept(comment ->log.info("created comment "+comment.htmlUrl()));

``` 

And endpoints related to teams and memberships are nested under the Organisation client:

```java
final TeamClient teamClient = organisationClient.createTeamClient();
teamClient.getMembership("username");
```

## Tracing

The GitHub client supports tracing via both OpenCensus and OpenTelemetry. Since OpenCensus is deprecated, we recommend
using OpenTelemetry. Using OpenTelemetry also enables context propagation when using this library.  
To enable tracing, you need to provide a tracer when initializing the client.

### OpenTelemetry

```java
import com.spotify.github.tracing.opentelemetry.OpenTelemetryTracer;

final GitHubClient githubClient =
        GitHubClient.create(baseUri, accessToken)
                // Uses GlobalOpenTelemetry.get() to fetch the default tracer
                .withTracer(new OpenTelemetryTracer());
```

You can also provide a custom `OpenTelemetry` object if you want to use a specific one.

```java
import com.spotify.github.tracing.opentelemetry.OpenTelemetryTracer;

final GitHubClient githubClient =
        GitHubClient.create(baseUri, accessToken)
                // Uses custom openTelemetry object to fetch the tracer
                .withTracer(new OpenTelemetryTracer(openTelemetry));
```

### OpenCensus

```java
import com.spotify.github.tracing.opencensus.OpenCensusTracer;

final GitHubClient githubClient =
        GitHubClient.create(baseUri, accessToken)
                // Uses Tracing.getTracer() to fetch the default tracer
                .withTracer(new OpenCensusTracer());
```

## Supported Java versions

This library is written and published with Java version 11. In our CI workflows, we execute
automated tests with the Java LTS versions 11, 17 and 21. Due to Java's backward compatibility,
this library can definitely be used in all the tested versions.

## Contributing

This project uses Maven. To run the tests locally, just run:

```bash
mvn clean verify
```

### Maintainers

#### Publishing a new version

If you are a maintainer, you can release a new version by just triggering the workflow
[prepare-release](./.github/workflows/prepare-release.yml) through the
[web UI](https://github.com/spotify/github-java-client/actions/workflows/prepare-release.yml).

- Select whether the new release should be a `major`, `minor` or `patch` release
- Trigger the release preparation on the `master` branch
- Pushes of this workflow will trigger runs of the
  [maven-release](https://github.com/spotify/github-java-client/actions/workflows/release.yml)
  workflow, which in turn will trigger the
  [github-release](https://github.com/spotify/github-java-client/actions/workflows/release-on-github.yml)
  workflow with the automatically created tag

The `prepare-release` workflow will also update the snapshot version in the `pom.xml` file to the next version. The
version which will be published to Maven Central will be the one specified in the `pom.xml` file (without the
`-SNAPSHOT` suffix).

#### Updating the GPG signing key

If you need to update the GPG signing key used for signing the releases when the existing key expires, you can do so by
following these steps:

1. Generate a new GPG key pair or use an existing one.
   If you don't have a GPG key pair, you can generate one using the following command:
    ```bash
    gpg --full-generate-key
    ```
   Follow the prompts to create your key pair. Make sure to remember the passphrase you set.
2. List your GPG keys to find the key ID:
   ```bash
   gpg --list-keys
   ```
   Look for the `pub` line, which will show the key ID in the format `XXXXXXXX`.
3. Export the public key to a file:
   ```bash
   gpg --armor --export <KEY_ID> > publickey.asc
    ```
4. export the private key to a file:
   ```bash
   gpg --armor --export-secret-key <KEY_ID> > privatekey.asc
    ```
5. Upload the private key to the GitHub repository secrets in `GPG_PRIVATE_KEY` and paste the contents of
   `privatekey.asc`.
6. Update the passphrase in the `GPG_PASSPHRASE` secret with the passphrase you set when generating the key.
7. Upload the public key to the OpenGpg key server at https://keys.openpgp.org/
8. Make sure to verify the public key with your email address on OpenGPG and that it is available on the key server.
9. Make sure that the release workflow is configured to use the `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE` secrets.
10. Run the release workflow to publish a new version of the library.

## Notes about maturity

This module was created after existing libraries were evaluated and dismissed, and we found that we were writing similar
code in multiple projects. As such, it at least initially only contains enough functionality for our internal
requirements
which reflects that we were working on build system integration with the GitHub pull requests. It has been widely used
for 4+
years. It's important to notice that it does not cover all GitHub v3 API. Adding missing endpoints should be very
straightforward.
Pull Requests are welcome.

## Code of conduct

This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this
code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
