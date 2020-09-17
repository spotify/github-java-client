![maven](https://github.com/spotify/github-client/workflows/maven/badge.svg)
[![codecov](https://codecov.io/gh/spotify/github-java-client/branch/master/graph/badge.svg?token=ADHNCIESSL)](https://codecov.io/gh/spotify/github-java-client)[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
![lifecycle: beta](https://img.shields.io/badge/lifecycle-beta-509bf5.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.spotify/github-client)


# github-client

A small Java library for talking to Github/Github Enterprise and interacting with projects.

It supports authentication via simple access tokens, JWT endpoints and Github Apps (via private key).

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

Start talking to Github API.

```java
final GitHubClient github = GitHubClient.create(URI.create("https://github.com/api/v3/"));
final IssueApi issueClient = github.createRepositoryClient("my-org", "my-repo").createIssueClient();
issueClient.listComments(ISSUE_ID).get().forEach(comment -> log.info(comment.body()));
```

## Authenticating

### Simple access token

```java
final GitHubClient github = GitHubClient.create(URI.create("https://github.com/api/v3/"));
// Do the requests
github.createRepositoryClient("my-org", "my-repo").getCommit("sha");
```

### Private key

To authenticate as a Github App, you must provide a private key and the App ID, together with the API URL.

```java
final GitHubClient github =
  GitHubClient.create(
    URI.create("https://github.com/api/v3/"),
    new File("/path-to-the/private-key.pem"),
    APP_ID);
```

Then, you can scope the client for a specific Installation ID, to do the operations at the installation level.
The client will manage the generation of JWT tokens, as well as requesting and caching the installation tokens
from GitHub.

```java
final GitHubClient scoped = GitHubClient.scopeForInstallationId(github, INSTALLATION_ID);
// Do the requests now using the scoped client.
scoped.createRepositoryClient("my-org", "my-repo").getCommit("sha");
```

It is also possible to provide the installation to the root client.

Refer to [Github App Authentication Guide](https://developer.github.com/apps/building-github-apps/authenticating-with-github-apps/) for more information.

## Usage

This library attempts to mirror the structure of GitHub API endpoints. As an example, to get details of a Commit, there is 
the `GET /repos/:owner/:repo/commits` API call, under the `repos` API. Therefore, the `getCommit` method lives in the RepositoryClient.

```java
final GitHubClient github = GitHubClient.create(URI.create("https://github.com/api/v3/"));
github.createRepositoryClient("my-org", "my-repo").getCommit("sha");
```

Some APIs, such as Checks API are nested in the Repository API. Endpoints such as `POST /repos/:owner/:repo/check-runs` live in the ChecksClient:

```java
final GitHubClient github =
  GitHubClient.create(
    URI.create("https://github.com/api/v3/"),
    new File("/path-to-the/private-key.der"),
    APP_ID);
// Checks API need to be used by Github Apps
GitHubClient.scopeForInstallationId(github, INSTALLATION_ID)
  .createRepositoryClient("my-org", "my-repo")
  .createChecksApiClient()
  .createCheckRun(CHECK_RUN_REQUEST);
``` 

## Contributing

This project uses Maven. To run the tests locally, just run:

```bash
mvn clean verify
```

## Notes about maturity

This module was created after existing libraries were evaluated and dismissed, and we found that we were writing similar
code in multiple projects. As such, it at least initially only contains enough functionality for our internal requirements
which reflect that we were working on build system integration with the Github pull requests. It has been widely used for 4+ 
years. It's important to notice that it does not cover all Github v3 API. Adding missing endpoints should be very straightforward.
Pull Requests are welcome.

## Code of conduct
This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are expected to honor this code.

[code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
