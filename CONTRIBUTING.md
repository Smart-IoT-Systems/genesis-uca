# Contributing to GeneSIS

The following is a set of guidelines for contributing to GeneSIS. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

**Table of Content**

# Code of Conduct

This project and everyone participating in it is governed by the GeneSIS [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. 

# Before Getting Started
**GeneSIS in the centre of [the ENACT H2020 project](https://www.enact-project.eu/)**

ENACT proposes to evolve DevOps methods and techniques to support the development and operation of smart IoT systems, which (i) are distributed, (ii) involve sensors and actuators and (iii) need to be trustworthy (\ie, trustworthiness refers to the preservation of security, privacy, reliability, resilience, and safety. 

GeneSIS is one of the core tool in the ENACT Framework making the link between Dev and Ops activities. In particular, in this framework, GeneSIS aims first at providing a language expressive enough for other tools to analyse and reason on the architecture of IoT systems (e.g., the actuation conflict manager leverage GeneSIS models to identify actuation conflicts and deploy actuation conflict managers, the Risk Managemement tool use GeneSIS models to analyse risks, etc.). Second, GeneSIS needs to support the reliable (rolling) deployment of IoT systems, including specific support for security, on infrastructures ranging from Cloud resources to Arduinos. Finally, GeneSIS has to offer support for dynamically adapting the deployment of an IoT systems and for monitoring its current status.

**GeneSIS publications**
> **[2020]** "_Continuous Deployment of Trustworthy Smart IoT Systems_", 
> Nicolas Ferry, Phu Nguyen, Hui Song, Erkuden Rios, Eider Iturbe, Angel Rego Fernandez, Satur Martinez 
> in Journal of Object Technology (JOT), special issue for ECMFA, AITO, 2020

> **[2019]** "_GeneSIS: Continuous Orchestration and Deployment of Smart IoT Systems_" 
> Nicolas Ferry, Phu H. Nguyen, Hui Song, Pierre-Emmanuel Novac, Stéphane Lavirotte, Jean-Yves Tigli, Arnor Solberg 
> Short paper in the proceedings of the IEEE COMPSAC conference, Milwaukee, USA, July 15-19, 2019 


**GeneSIS and Packages**

**Package Conventions**

**Design Decisions**


# Reporting Bugs

Bugs are tracked as GitLab issues. 
Explain the problem and include additional details to help maintainers reproduce the problem:
* Use a clear and descriptive title for the issue to identify the problem.
* Describe the exact steps which reproduce the problem
* Explain which behavior you expected to see instead and why.
* Which version of GeneSIS are you using?
* Which OS and set up are you using?

Labels for bugs if needed

# Submitting Changes

Please send a [Pull Request to GeneSIS](https://gitlab.com/enact/GeneSIS/-/merge_requests) with a clear list of what you've done. Please make sure all of your commits are atomic (one feature per commit).

Always write a clear log message for your commits. One-line messages are fine for small changes, but bigger changes should look like this:

    $ git commit -m "A brief summary of the commit
    > 
    > A paragraph describing what changed and its impact."

Labels for changes if needed

