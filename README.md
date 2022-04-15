![maven](https://github.com/spotify/github-client/workflows/maven/badge.svg)
[![codecov](https://codecov.io/gh/spotify/github-java-client/branch/master/graph/badge.svg?token=ADHNCIESSL)](https://codecov.io/gh/spotify/github-java-client)[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![lifecycle: beta](https://img.shields.io/badge/lifecycle-beta-509bf5.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.spotify/github-client)


# github-client

A small Java library for talking to GitHub/GitHub Enterprise and interacting with projects.

It supports authentication via simple access tokens, JWT endpoints and GitHub Apps (via private key).

It is also very light on GitHub, doing as few requests as necessary.

## Getting Started

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

Refer to [GitHub App Authentication Guide](https://developer.github.com/apps/building-github-apps/authenticating-with-github-apps/) for more information.

## Usage

This library attempts to mirror the structure of GitHub API endpoints. As an example, to get details of a Commit, there is 
the `GET /repos/:owner/:repo/commits` API call, under the `repos` API. Therefore, the `getCommit` method lives in the RepositoryClient.

```java
final RepositoryClient repositoryClient = githubClient.createRepositoryClient("my-org", "my-repo");
log.info(repositoryClient.getCommit("sha").get().htmlUrl());
```

Some APIs, such as Checks API are nested in the Repository API. Endpoints such as `POST /repos/:owner/:repo/check-runs` live in the ChecksClient:

```java
final ChecksClient checksClient = repositoryClient.createChecksApiClient();
checksClient.createCheckRun(CHECK_RUN_REQUEST);

final IssueClient issueClient = repositoryClient.createIssueClient();
issueClient.createComment(ISSUE_ID, "comment body")
  .thenAccept(comment -> log.info("created comment " + comment.htmlUrl()));
``` 

## Contributing

This project uses Maven. To run the tests locally, just run:

```bash
mvn clean verify
```

If you are a maintainer, you can release a new version by running this command locally: `mvn release:prepare`

## Notes about maturity

This module was created after existing libraries were evaluated and dismissed, and we found that we were writing similar
code in multiple projects. As such, it at least initially only contains enough functionality for our internal requirements
which reflect that we were working on build system integration with the GitHub pull requests. It has been widely used for 4+ 
years. It's important to notice that it does not cover all GitHub v3 API. Adding missing endpoints should be very straightforward.
Pull Requests are welcome.

## Code of conduct
This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
