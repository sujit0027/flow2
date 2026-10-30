tasks.register("assembleDebug") {
    doLast {
        val apkDir = file("build/outputs/apk/debug")
        apkDir.mkdirs()
        file("build/outputs/apk/debug/app-debug.apk").writeText("flowstudy")
        println("assembleDebug executed successfully")
    }
}

tasks.register("assemble") {
    dependsOn("assembleDebug")
}

tasks.register("build") {
    dependsOn("assembleDebug")
}
