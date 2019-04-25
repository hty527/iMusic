* 代码库本地构建配置
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
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

uploadArchives{
    repositories.mavenDeployer{
        // 配置本地仓库路径，这里是项目的根目录下的maven目录中
        repository(url: uri('../maven'))
        // 唯一标识 一般为模块包名 也可其他
        pom.groupId = "com.android.imusic.lib"
        // 项目名称（一般为模块名称 也可其他
        pom.artifactId = "video-player-lib"
        // 发布的版本号
        pom.version = "1.0.0"
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:26.1.0'
}
```
工程根目录下build.gradle文件配置
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
            url 'file://D://AndroidStudioProjects//IMusic//maven'
        }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```