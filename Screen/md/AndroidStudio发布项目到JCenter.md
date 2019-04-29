* 转载请注明出处，谢谢！
###写在前面：
&emsp; &emsp; 之前写项目时，喜欢将重复的功能或者自定义控件封装到公用的模块中。当多个项目都依赖这个公共模块时，其中某一个项目中对模块中的代码做了修改，此时其他依赖该模块的项目不得不做出修改或者同步代码，相当麻烦。而此时有没有什么办法能避免一处改动，四方同步呢？答案肯定是有的。这里我们就跟随着前辈们的脚步来学习如何将项目模块发布至Maven仓库。真正做到只需一处修改，四处升级即可。本文将介绍发布到Maven的三种途径。教程开始前，先了解下Maven是干嘛的。
###1：Maven是啥？
简单来说Maven是项目管理工具，其内部工作机制是通过解析pom.xml文件中的配置获取jar包下载地址自动将jar到Peoject中，省去了手动下载到本地的繁琐过程。
###2：Maven在哪里？
目前Android比较著名的代码仓库是mavenCentral和jcenter，早期版本的Studio默认仓库是mavenCentral，后期版本默认仓库更换成了jcenter。Maven项目管理工具就在这两个仓库上。
##代码仓库的三种发布方式
### 一：发布仓库到本地
#### 1：uploadArchives配置和Task生成
在要发布的模块build.gradle中配置uploadArchives属性，示例代码：
```
//添加这一行，告诉gradle应用到项目中
apply plugin: 'maven'
//配置模块未本地Maven仓库
uploadArchives{
    repositories.mavenDeployer{
        // 配置本地仓库路径，这里是项目的根目录下的maven目录中
        repository(url: uri('../maven'))
        // 唯一标识 一般为模块包名 也可其他
        pom.groupId = "com.android.imusic.player.lib"
        // 项目名称（一般为模块名称 也可其他
        pom.artifactId = "music-player-lib"
        // 发布的版本号
        pom.version = "1.0.0"
    }
}
```
![uploadArchives配置截图图示](https://upload-images.jianshu.io/upload_images/16585967-7058b314d9fda540.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
配置后Sync Now一把，Studio编辑器右上角Gradle中你要发布的模块下会多出一个upload目录，目录中有个uploadArchives任务脚本，这个脚本就是将库发布到Maven仓库的Task。
#### 2：执行uploadArchives任务
点击如图所示的uploadArchives
![uploadArchives在Studio中图示](https://upload-images.jianshu.io/upload_images/16585967-f778035dae1bff70.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
uploadArchives有两种运行方法：1：双击运行。2：在AndroidStudio自带的Terminal中执行 gradlew uploadArchives 命令运行。根据喜好选取一种即可，开始任务后等待编译完成。
![编译到Maven成功图示](https://upload-images.jianshu.io/upload_images/16585967-72b7a7ae79180119.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
编译完成后在你项目的根目录会生成一个maven目录，在目录maven->com.android.imusic.ib->music-player-lib->1.0.0中会有个music-player-lib.aar 文件，这个文件就是打包签名好的库文件。
![模块编译到Maven完成图示](https://upload-images.jianshu.io/upload_images/16585967-d58ee6ddb27ace24.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 3：应用music-player-lib.aar库文件到项目中
* 3.1：项目根目录build.gradle配置Maven
在你的项目中根目录下的build.gradle中配置如下代码：
```
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
```
* 3.2：app中的build.gradle配置
在你想要依赖.aar的模块中的build.gradle中配置如下代码：
```
//应用这个依赖,这里填写刚才配置的包名+库名
implementation 'com.android.imusic.lib:music-player-lib:1.0.0'
```
* 或者你可以不配置本地仓库，直接将.aar文件复制到你的app模块中直接使用，配置代码如下：
```
//在你的app中的build.gradle中配置
android {
        repositories {
            flatDir {
                dirs 'libs'
            }
        }
    }
//添加依赖
implementation(name: 'music-player-lib-1.0.0', ext: 'aar')
```
点击Sync Now等待完成后发布依赖到本地Maven就成功啦！在发布到本地Maven前你应该要校验混淆开启的情况噢~免得他人依赖你的项目时掉坑里去了。发布到本地Maven固然方便，但是公司项目团队人数较多时，将模块打包成.aar文件后复制到项目多少有点不方便，比如所A程序员的本地Maven路径是在D盘，而B程序员的Maven路径又在E盘，这样同步代码改来改去着实难受，所以接下来就介绍适应公司内部团队多人开发的场景，即将代码发布至局域网仓库。
### 二：发布仓库到局域网
局域网部署需要用到私服部署，这里跟随前辈脚步介绍使用Nexus来搭建部署局域网Maven仓库。
#### 1：安装部署Nexus(这里用nexus-2.14.2-01版本演示)
[点此前往下载nexus-2.14.2-01](https://sonatype-download.global.ssl.fastly.net/repository/repositoryManager/oss/nexus-2.14.8-01-bundle.zip)
下载解压后会看到目录下下面几个文件夹
![nexus目录文件预览图示](https://upload-images.jianshu.io/upload_images/16585967-af2dbc176a479a59.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
目录下批处理脚本释义：
console-nexus.bat--->常规安装Nexus服务
install-nexus--->启动开机自启动Nexus服务
start-nexus.bat--->启动Nexus服务
stop-nexus.bat--->停止Nexus服务
uninstall-nexus.bat--->卸载Nexus组件
wrapper.exe--->桌面程序
* 鼠标右键"console-nexus.bat"以管理员身份运行，等待CMD创建安装完成后在浏览器输入：http://localhost:8081/nexus。
安装和成功启动后是这样的
![Nexus启动默认界面图示](https://upload-images.jianshu.io/upload_images/16585967-d31e7b5665054d9d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 2：Nexus仓库配置
* 2.1：点击右上角Log In，登录默认账号,默认账号：admin   密码：admin123
![Maven登录图示](https://upload-images.jianshu.io/upload_images/16585967-cfff9f9489a8316f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 2.2：修改默认密码
![Maven账号默认密码修改图示](https://upload-images.jianshu.io/upload_images/16585967-f64ae475f00a4f1a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
按照步骤修改密码即可,如果需要修改默认的端口号，修根目录下的conf-nexus.properties文件中的application-port=8081一栏，将8081替换你指定的端口号：、
![Nexus端口号修改图示](https://upload-images.jianshu.io/upload_images/16585967-8d5a464e9a358663.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
配置就绪后，准备发布，发布后你的项目路径如图所示：
![Nexus项目存储Maven管理目录](https://upload-images.jianshu.io/upload_images/16585967-019c1db8a0cc74b8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 3：准备发布
* 3.1：模块的build.gradle配置
```
apply plugin: 'com.android.library'
//应用到Maven
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
```
* 3.2：执行uploadArchives，将项目发布到Nexus
执行uploadArchives或者使用Studio自带的Terminal中执行 gradlew uploadArchives 命令开始构建运行。等待结束后，浏览器打开[Nexus](http://localhost:8081/nexus)，查看Repositories->Releases 目录。成功发布后的代码库模块目录如图所示：
![发布代码到Nexus成功图示](https://upload-images.jianshu.io/upload_images/16585967-f9851919635429e3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 4：使用局域网Nexus存储库代码库
* 4.1：根build.gradle配置修改
在你的项目中的根build.gradle中添加如下代码
```
allprojects {
    repositories {
        google()
        jcenter()
        //本地Maven仓库地址,取决于你的磁盘目录
        maven {
            //这里本地不再使用，改用下面的局域网的路径
            //url 'file://D://AndroidStudioProjects//IMusic//maven',
            //添加本地仓库URL
            url 'http://localhost:8081/nexus/content/repositories/releases/'
        }
    }
}
```
* 4.2：app模块中build.gradle配置修改
在你的项目中要使用代码库的模块中的build.gradle做如下修改：
```
dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    //和刚才使用本地Maven仓库一致：包名+库名
    implementation 'com.android.imusic.lib:music-player-lib:1.0.0'
```
到此，发布代码到本地仓库到此结束了。但是随之又来了一个问题，局域网仓库对公司团队来说固然是好，但是你开发的模块很牛逼的时候，别人要用你的模块时怎么办？这个时候就需要将模块发布至外网仓库，即发布代码至JCenter。
### 三：发布代码到JCenter
在早期的AndroidStudio版本中，Google默认使用的仓库是mavenCentral，但由于发布流程太复杂(前辈们是这样分析的，不知道对不对)，后来改用默认仓库是JCenter,说了这么久还没介绍JCenter是什么？简单来说JCenter是Bintray其下的一个分区仓库，代码托管的存储空间。那既然JCenter是属于Bintray旗下的，那就首先得从Bintray开始。
#### 1：Bintray账号准备
Bintray是一家OSS服务商，类似国内的阿里云OSS。要将代码发布之JCenter，必须先有Bintray账号和分区目录。感兴趣可以去看下AndroidStudio的Bintray库在Giuhub中的项目：[Bintray-Github](https://github.com/novoda/bintray-release)。注册账号我们注册个人账号即可。[前往Bintray官网个人账号注册](https://bintray.com/signup/oss)。
* 注意：1.注册账号最好注册个人的，不然企业账号没有add To Jcenter功能，需要收费。2：注册的邮箱一定要使用国外的邮箱，不然注册没反应。
![Bintray账号注册图示](https://upload-images.jianshu.io/upload_images/16585967-5f4354cc092e7fb1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 2：为Bintray账号添加组织
添加组织有两种方式：1：创建Bintray组织。2：导入第三方托管平台组织。
##### 2.1：创建Bintray组织
在bintray主页鼠标放至头像弹窗的Menu框中点击createOrganization->界面跳转后点击->Create new organization创建新的组织。
![创建组织入口图示](https://upload-images.jianshu.io/upload_images/16585967-fc7db3a5b6cf1bc9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
按照提示填写组织基本信息提交即可。
![Bintray组织资料图示](https://upload-images.jianshu.io/upload_images/16585967-210419ac6112bb15.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
##### 2.2：导入第三方平台组织
Bintray支持第三方托管平台的组织导入，但目前为止只支持从Github导入组织到Bintray。
* 2.2.1：导入Github组织
在bintray主页鼠标放至头像弹窗的Menu框中点击createOrganization->再点击Import from GitHub导入Github账号组织。第一次导入官方会给出操作流程图，如下：
![Bintray导入Github组织流程图示](https://upload-images.jianshu.io/upload_images/16585967-d540e259d6dc80d2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 2.2.1.1 前往Github设置中心
点击图中标记的Account Setting page，前往Github设置中心界面。跳转至Github的设置中心后依次点击左侧的Applications，展开界面后点击右侧的Authorized OAuth Apps，展开列表就可看到支持列表中有bintray了。
![授权设置及操作图示](https://upload-images.jianshu.io/upload_images/16585967-24d7ff5730dbd200.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 2.2.1.2 授权Bintray访问Github账户的权限
点击Bintray一栏中的Grant，授予Bintray访问Github账户的权限。
![Github授权Bintray访问权限图示](https://upload-images.jianshu.io/upload_images/16585967-96ee0e94e6cf2f80.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
* 2.2.2：填写第三方组织在Bintray中的基本信息
授予访问权限后，再次走导入Github组织流程，选择你的组织后，根据界面提示填写基本信息提交即可。
#### 3：创建Bintray存储分区
点击Add New Repository创建存储分区
![创建存储分区入口图示](https://upload-images.jianshu.io/upload_images/16585967-30c4d63c4b2d8dba.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
按图中所示，填写分区基本信息
![填写分区基本信息图示](https://upload-images.jianshu.io/upload_images/16585967-b8683a0de48f75d1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
创建成功后是这样的
![存储分区建成功](https://upload-images.jianshu.io/upload_images/16585967-3fe20347fcd900db.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 4：模块build.gradle配置
##### 4.1：在你项目的根build.gradle中配置bintray环境依赖
```
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        //添加bintray环境
        classpath 'com.novoda:bintray-release:0.8.1'
    }
}
```
#####  4.2：在你要发布的模块build.gradle中配置如下配置
```
apply plugin: 'com.novoda.bintray-release'

//推送到Bintray配置
publish {
    //你的账户bintray下某个组织id
    userOrg = 'xxxxxx'
    //Maven仓库下库的包名,一本与包名相同
    groupId = 'xxx.xxx.xxx'
    //项目名称
    artifactId = 'xxx'
    //版本号
    publishVersion = '1.0.0'
    //项目介绍，可以不写
    desc = 'xxx'
    //项目主页，可以不写
    website = 'xxx'
}
```
#### 5：编译并发布到Bintray
##### 5.1：获取Bintray API Key
在你的Bintray主页点击右上角头像 右上角用户名–>Edit Your Profile -> API Key –>输入密码–>Submit–>Show。复制API key备用。
![Bintray获取API KEY图示](https://upload-images.jianshu.io/upload_images/16585967-7db2fb14d949add3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
##### 5.2：运行发布命令
在AndroidStudio编译器自带的Terminal中输入命令：
```
gradlew clean build bintrayUpload -PbintrayUser=BINTRAY_USERNAME -PbintrayKey=BINTRAY_KEY -PdryRun=false
//BINTRAY_USERNAME替换为你的Bintray用户名，BINTRAY_KEY替换为你刚才获取的Bintray API KEY
//PdryRun释义：false：编译且将你的项目上传至Bintray ，fasle：只是构建你的项目，不会上传
```
如果你在编译中遇到问题，请前往[AndroidStudio发布项目到Bintray遇到的错误及解决](https://www.jianshu.com/p/6b272fe9bd28)
开始构建后耐心等待，可能会因为socket出现多次上传失败，重试即可。直到出现这个表示上传成功。
![发布到Bintray成功](https://upload-images.jianshu.io/upload_images/16585967-904068a81dbd3481.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
上传完成后可在你的主页或者组织下查看
![上传到Bintray成功图示](https://upload-images.jianshu.io/upload_images/16585967-30d77394039283c4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 6：添加至JCenter
点击右下角add to JCenter，确认后点击send提交，等待审核通过。
![添加项目至JCenter图示](https://upload-images.jianshu.io/upload_images/16585967-8aeba888fd9af504.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
send后，等待审核通过，通过后即可在外网使用你的库作为依赖了。
![发送代码到JCenter信息填写图示](https://upload-images.jianshu.io/upload_images/16585967-f424ff1da4c29456.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 7：项目引用JCenter库
##### 7.1未添加至JCenter引用：
* 7.1.1：在根build.gradle中配置如下代码
```
allprojects {
    repositories {
        //bintray环境
        maven { url 'https://dl.bintray.com/novoda-oss/snapshots/' }
        //你的maven路径
        maven { url 'https://dl.bintray.com/你的id/maven' }
    }
}
```
* 7.1.2：在app模块中的build.gradle中配置如下代码
```
dependencies {
    implementation project(':video-player-lib')
}
```
##### 7.2：已添加至JCenter引用：
* 7.2.1：在根build.gradle中配置如下代码
```
allprojects {
    repositories {
        //bintray环境
        maven { url 'https://dl.bintray.com/novoda-oss/snapshots/' }
        //添加支持JCenter即可
        jcenter()
    }
}
```
* 7.2.2：在app模块中的build.gradle中配置如下代码
```
dependencies {
    implementation project(':video-player-lib')
}
```
##至此，全部Maven及JCenter发布流程完毕，祝您玩的愉快~~