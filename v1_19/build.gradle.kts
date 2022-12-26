import net.blueberrymc.blueberryfarm.blueberry

plugins {
    id("net.blueberrymc.blueberryfarm") version("2.0.0-SNAPSHOT") // https://github.com/BlueberryMC/BlueberryFarm
}

blueberry {
    minecraftVersion.set("1.19.3")
    apiVersion.set("1.7.0-SNAPSHOT")
}

dependencies {
    implementation(project(":common"))
    blueberry()
}

tasks {
    withType<net.blueberrymc.blueberryfarm.tasks.RunClient> {
        this.addArgs("--mixin mixins.viablueberry.json")
    }

    withType<net.blueberrymc.blueberryfarm.tasks.RunServer> {
        this.addArgs("--mixin mixins.viablueberry.json")
    }
}
