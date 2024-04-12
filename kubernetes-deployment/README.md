# README

**该目录下存放kubernetes集群的资源部署yaml文件，下面是项目运行方式：**

- 克隆本项目到本地机器:`git clone https://github.com/InSightHealth/server-backend.git`
- 执行命令`clean install -Dmaven.test.skip=true -f serverbackend/pom.xml`，构建Maven父级项目。
- 找到项目下的app-gateway，user和storage三个微服务，根据其目录下的dockerFile文件，利用Maven提供的Docker插件(需要依赖Docker环境)进行镜像打包。
- 这里以构建app-gateway镜像为例，执行命令`clean install -Dmaven.test.skip=true dockerfile:build -f serverbackend/springcloudgateway/app-gateway/pom.xml`打包得到最新镜像。
- 将打包完成的镜像推送至自己的私有仓库，从而完成kubernetes集群应用的部署。