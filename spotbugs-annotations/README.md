# SpotBugs Annotations

[![Maven Central](https://img.shields.io/maven-central/v/com.github.spotbugs/spotbugs-annotations.svg)](https://central.sonatype.com/artifact/com.github.spotbugs/spotbugs-annotations)
[![Javadocs](http://javadoc.io/badge/com.github.spotbugs/spotbugs-annotations.svg)](http://javadoc.io/doc/com.github.spotbugs/spotbugs-annotations)

## Troubleshooting Gradle Resolution Issues

If you encounter errors resolving `spotbugs-annotations` (especially for versions 4.8.6 to 4.9.x), please check the following:

1. **Repository Configuration**
   ```groovy
   repositories {
       mavenCentral()
   }
   ```

2. **Dependency Declaration**
   ```groovy
   dependencies {
       implementation 'com.github.spotbugs:spotbugs-annotations:4.10.0'
   }
   ```

3. **Gradle Version**
   - Use Gradle 6.x or newer for best compatibility.

4. **Clear Gradle Cache**
   - Run: `gradle --refresh-dependencies`

5. **Network/Proxy Issues**
   - Ensure your environment can access Maven Central.

6. **Check for Typos**
   - Validate groupId, artifactId, and version.

If you still encounter issues, please report them with your Gradle version and build configuration.