dependencies {
    compileOnly("net.blueberrymc:blueberry-api:1.7.0-SNAPSHOT")
    compileOnly("org.apache.logging.log4j:log4j-api:2.19.0")
    compileOnly("io.netty:netty-all:4.1.82.Final")
}

configurations.all {
    // download blueberry-api every time
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}
