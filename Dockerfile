# Copyright (c) Bosch.IO GmbH 2020.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0

#
# Build stage
#
FROM maven:3.3.9-jdk-8-alpine AS ANTENNA_MAVEN_BUILD

COPY . /home/antenna/
WORKDIR /home/antenna/
RUN mkdir /maven
RUN mvn -Dmaven.repo.local=/maven install -DskipTests -Dskip.pmd=true -pl '!core/frontend-stubs-testing'

#
# Package stage
#
FROM openjdk:8-jre-slim
WORKDIR ../..
COPY --from=ANTENNA_MAVEN_BUILD /home/antenna/assembly/cli/target/antenna.jar /usr/local/lib/antenna.jar
RUN rm -rf /home
RUN rm -rf /maven

COPY /example-projects/example-project/pom.xml /antenna/antennaConfiguration.xml
COPY /example-projects/example-project/src /antenna/src

ENTRYPOINT ["java", "jar", "/usr/local/lib/antenna.jar"]
CMD ["/antenna/antennaConfiguration.xml"]
