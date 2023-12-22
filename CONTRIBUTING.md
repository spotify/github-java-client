# Contributing to the github-java-client

Thanks for your interest in the github-java-client. Our goal is to bring a reliable Java-based 
alternative to the GitHub API.

## Getting Started

The Github Java Clients's [open issues are here](https://github.com/github-java-client/github-java-client/issues). 
In time, we'll tag issues that would make a good first pull request for new contributors. An easy 
way to get started helping the project is to *file an issue*. Issues can include bugs to fix, 
features to add, or documentation that looks outdated.

This library is maintained by @spotify/gjc-maintainers. If you have any questions, issues or need a 
review, please tag this team in the relevant PR/issue.

## Contributions

This project welcomes contributions from everyone.

Contributions to github-java-client should be made in the form of GitHub pull requests. Each pull 
request will be reviewed by a maintainer of the library and either merged and released or given 
feedback for changes that would be required.

## Pull Request Checklist

- Branch from the master branch and, if needed, rebase to the current master branch before 
  submitting your pull request. If it doesn't merge cleanly with master you may be asked to rebase 
  your changes.
- Commits should be as small as possible while ensuring that each commit is valid independently
  (i.e. each commit should compile and the tests should pass).
- Add tests relevant to the fixed bug or new feature. We love to increase our test coverage so any 
  contributions made to improving that will be very welcomed.

## Coding Standards

- This library is modelled after the [GitHub API](https://docs.github.com/en/rest?apiVersion=2022-11-28) and it has been structured to mimic that. 
  For example, to access the Teams endpoints, you need to instantiate an `OrganisationClient`  
  and then a `TeamsClient` as [seen here](./src/main/java/com/spotify/github/v3/clients/OrganisationClient.java). This mirrors the nested structure of the API endpoints such as the 
  [/orgs/{org}/teams/{team_slug}](https://docs.github.com/en/rest/teams/teams?apiVersion=2022-11-28#list-teams) endpoint
- We operate a monkey see, monkey do approach to this library. We understand that there are some inconsistencies in the library 
  in terms of how the tests and/or endpoints are written but we, with your help, are working on creating a more consistent codebase.
- All bug fixes and new features need to be fully tested.