* 代码库本地构建配置，需要电脑安装Nexus，启动服务后制定编译发布到一局域网仓库<br>
本地局域网仓库地址http://localhost:8081/nexus/#view-repositories;releases~browsestorage

* 双击 upload 下的uploadArchives 或者 在Terminal中输入gradlew uploadArchives 命令编译代码库
```
apply plugin: 'com.android.library'
apply plugin: 'maven'

android {
    compileSdkVersion 26
    defaultConfig {
        minSdkVersion 16
        targetSdkVersion 26
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
}

//打包main目录下代码和资源的 task
task androidSourcesJar(type: Jar) {
    classifier = 'sources'
    from android.sourceSets.main.java.srcDirs
}
//配置需要上传到maven仓库的文件
artifacts {
    archives androidSourcesJar
}
//上传到Maven仓库的task
uploadArchives {
    repositories {
        mavenDeployer {
            //指定maven的仓库url，IP+端口+目录
            repository(url: "http://localhost:8081/nexus/content/repositories/releases/") {
                //填写你的Nexus的账号密码
                authentication(userName: "admin", password: "123456")
            }
            // 唯一标识 一般为模块包名 也可其他
            pom.groupId = "com.android.imusic.lib"
            // 项目名称（一般为模块名称 也可其他
            pom.artifactId = "music-player-lib"
            // 发布的版本号
            pom.version = "1.0.0"
        }
    }
}

dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation 'com.android.support:appcompat-v7:26.1.0'
    implementation 'com.android.support:recyclerview-v7:26.1.0'
    implementation 'com.android.support:design:26.1.0'
    implementation 'com.android.support.constraint:constraint-layout:1.0.2'
    implementation 'com.github.bumptech.glide:glide:3.7.0'
    implementation 'jp.wasabeef:glide-transformations:2.0.1'
}
```
根目录build.gradle文件配置
```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
//应用Config配置文件
apply from: "config.gradle"

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        //本地Maven仓库地址,取决于你的磁盘目录
        maven {
            url 'file://D://AndroidStudioProjects//IMusic//maven',
            //添加本地仓库URL
            url 'http://localhost:8081/nexus/content/repositories/releases/'
        }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```