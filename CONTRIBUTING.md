# Contributing to GeneSIS

The following is a set of guidelines for contributing to GeneSIS. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

#### Table Of Contents
[Code of Conduct](#code-of-conduct)

[About GeneSIS](#about-genesis)
  * [GeneSIS in the centre of the ENACT H2020 project](#genesis-in-the-centre-of-the-enact-h2020-project)
  * [GeneSIS publications](#genesis-publications)
  * [GeneSIS and Packages](#genesis-and-packages)
  * [Design Decisions](#design-decisions)

[Reporting Bugs](#reporting-bugs)

[Submitting Changes](#submitting-changes)
  * [Pull Request Labels](#pull-request-labels)


## Code of Conduct

This project and everyone participating in it is governed by the GeneSIS [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. 


## About GeneSIS

### GeneSIS in the centre of the ENACT H2020 project

[ENACT](https://www.enact-project.eu/) proposes to evolve DevOps methods and techniques to support the development and operation of smart IoT systems, which (i) are distributed, (ii) involve sensors and actuators and (iii) need to be trustworthy (_i.e._, trustworthiness refers to the preservation of security, privacy, reliability, resilience, and safety). 

GeneSIS is one of the core tool in the ENACT Framework making the link between Dev and Ops activities. In particular, in this framework, GeneSIS aims first at providing a language expressive enough for other tools to analyse and reason on the architecture of IoT systems (e.g., the actuation conflict manager leverage GeneSIS models to identify actuation conflicts and deploy actuation conflict managers, the Risk Managemement tool use GeneSIS models to analyse risks, etc.). Second, GeneSIS needs to support the reliable (rolling) deployment of IoT systems, including specific support for security, on infrastructures ranging from Cloud resources to Arduinos. Finally, GeneSIS has to offer support for dynamically adapting the deployment of an IoT systems and for monitoring its current status.

### GeneSIS publications
> **[2020]** "_[Continuous Deployment of Trustworthy Smart IoT Systems](http://dx.doi.org/10.5381/jot.2020.19.2.a16)_", 
> Nicolas Ferry, Phu Nguyen, Hui Song, Erkuden Rios, Eider Iturbe, Angel Rego Fernandez, Satur Martinez 
> in Journal of Object Technology (JOT), special issue for ECMFA, AITO, 2020

> **[2019]** "_[GeneSIS: Continuous Orchestration and Deployment of Smart IoT Systems](https://doi.ieeecomputersociety.org/10.1109/COMPSAC.2019.00127)_" 
> Nicolas Ferry, Phu H. Nguyen, Hui Song, Pierre-Emmanuel Novac, Stéphane Lavirotte, Jean-Yves Tigli, Arnor Solberg 
> Short paper in the proceedings of the IEEE COMPSAC conference, Milwaukee, USA, July 15-19, 2019 


### GeneSIS and Packages

The GeneSIS repository is organized in three main folders:
* The `docs` folder contains all the GeneSIS documentation, this includes: tutorials, examples of deployment models, the documentation wiki, and ENACT specific examples and development.
* The `Dockerfiles` folder contains the different docker files to build the images of GeneSIS and of its deployment agent both for `arm` and `amd` architectures.
* The `src` folder contains the code of GeneSIS. We detail its structure in the following.

The GeneSIS code is organized in 5 modules:
* The GeneSIS server (i.e., the deployment engine) can be found in the `engine` folder. This includes the core of the engine at the root of the folder, and a set of connectors for the engine to interact with different type of resources and customize the deployment according to these resources (e.g., Docker, SSH, Node-Red, Ansible, ThingML).
* The `metamodel` of the GeneSIS language as well as all the facilities to manipulate the model can be found in the `metamodel` folder.
* The `public` and `ui` folders contains all the code of the GeneSIS client (i.e., the GeneSIS web interface). The `public` folder contains static files publicly available whilst the `ui` folder contains the dynamic code.
* The `repository` folders contains a set of specific GeneSIS component types. New component type descriptions can be added to this folder and will be dynamically loaded at starting time by the GeneSIS engine, making them available for deployment. 

### Design Decisions

In the following we list some of the GeneSIS design decisions:
* The UI is implemented using a React-based framework named [ant.design](https://ant.design). This cannot be changed as this to all ENACT tools.
* GeneSIS models are serialized in JSON. This cannot be changed as this has been agreed by the ENACT partners.
* The GeneSIS execution engine send notification about the status of deployment process as well as about the status of the system deployed. These notifications are sent using MQTT and the messages exchanged are serialized in JSON. This mechanism is used by the GeneSIS UI to provide end user with details about the status of a deployment.
* The GeneSIS execution engine can be controlled via a REST-based API. This API is documented with swagger and is accessible at the following address: `http://<ip_genesis>:8880/api-docs`. By making the documentation available locally from the GeneSIS engine, we ensure end user always access a documentation in line with their version of GeneSIS.
* The GeneSIS metamodel is written in pure javascript and object are implemented as functions with functional inheritence. 

## Reporting Bugs

Bugs are tracked as GitLab issues. 
When submitting an issue, please explain the problem and include additional details to help maintainers reproduce the problem. This typically means:
* Use a clear and descriptive title for the issue to identify the problem.
* Describe the exact steps which reproduce the problem
* Explain which behavior you expected to see instead and why.
* Which version of GeneSIS are you using?
* Which OS and set up are you using?

Labels for bugs if needed

## Submitting Changes

Please send a [Pull Request to GeneSIS](https://gitlab.com/enact/GeneSIS/-/merge_requests) with a clear list of what you've done. Please make sure all of your commits are atomic (one feature per commit).

Always write a clear log message for your commits. One-line messages are fine for small changes, but bigger changes should look like this:

    $ git commit -m "A brief summary of the commit
    > 
    > A paragraph describing what changed and its impact."

#### Pull Request Labels

| Label name | `genesis/genesis` :mag_right: | `genesis`‑org :mag_right: | Description
| --- | --- | --- | --- |
| `work-in-progress` | [search][search-genesis-repo-label-work-in-progress] | [search][search-genesis-org-label-work-in-progress] | Pull requests which are still being worked on, more changes will follow. |
| `needs-review` | [search][search-genesis-repo-label-needs-review] | [search][search-genesis-org-label-needs-review] | Pull requests which need code review, and approval from maintainers or Genesis core team. |
| `under-review` | [search][search-genesis-repo-label-under-review] | [search][search-genesis-org-label-under-review] | Pull requests being reviewed by maintainers or Genesis core team. |
| `requires-changes` | [search][search-genesis-repo-label-requires-changes] | [search][search-genesis-org-label-requires-changes] | Pull requests which need to be updated based on review comments and then reviewed again. |
| `needs-testing` | [search][search-genesis-repo-label-needs-testing] | [search][search-genesis-org-label-needs-testing] | Pull requests which need manual testing. |

