import net.blueberrymc.blueberryfarm.blueberry

plugins {
    id("net.blueberrymc.blueberryfarm") version("2.2.0") // https://github.com/BlueberryMC/BlueberryFarm
}

blueberry {
    minecraftVersion.set("1.19.4")
    apiVersion.set("1.8.0-SNAPSHOT")
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
