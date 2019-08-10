
![doodles](https://www.google.com/logos/doodles/2016/teachers-day-2016-us-6296626244091904.2-hp2x.gif)

----
[![码云Gitee](https://gitee.com/binary/weixin-java-tools-springmvc/badge/star.svg?theme=blue)](https://gitee.com/binary/weixin-java-tools-springmvc)
[![Github](http://github-svg-buttons.herokuapp.com/star.svg?user=Wechat-Group&repo=weixin-java-demo-springmvc&style=flat&background=1081C1)](https://github.com/Wechat-Group/weixin-java-demo-springmvc)
[![Build Status](https://travis-ci.org/Wechat-Group/weixin-java-demo-springmvc.svg?branch=master)](https://travis-ci.org/Wechat-Group/weixin-java-demo-springmvc)

This repository is a wechat springmvc demo based on **WxJava**

本仓库的项目是一个基 于**[WxJava](https://github.com/wechat-group/WxJava)** 的springmvc Demo.

新手遇到问题，请务必先阅读[【开发文档首页】](https://github.com/Wechat-Group/WxJava/wiki)的常见问题部分，可以少走很多弯路，节省不少时间。


## 本项目目前实现了如下功能：

* 与微信服务器的AES加密通信
* 微信支付
* 商户号向个人用户转账
* 自定义菜单
* 关注公众号、客服消息路由处理
* 模版消息
* 通过openid获取用户基本信息
* 通过code获得基本用户信息


Welcome to Pull Requests!

欢迎大家积极Pull Requests来丰富此Demo的功能！

## 快速使用：

**将项目下载到本地**
```shell
git clone https://github.com/wechat-group/weixin-java-demo-springmvc
```

**配置公众号信息**

打开`src/main/resources/wx.properties`文件，配置公众号相关信息。

**Maven打包**
```shell
mvn war:war
```

**上传至服务器**

必须使用服务器的80端口才能与微信服务器进行交互。

**微信公众平台服务器配置**

进入到**微信公众平台**的`开发/基本配置`页面，配置`URL(服务器地址)`、`Token(令牌)`、`EncodingAESKey(消息加解密密钥）`。
```shell
URL(服务器地址)：http://192.168.1.1/wechatTestService/core
```

## License
- 本项目的所有代码除另有说明外,均按照 [MIT License](https://github.com/racaljk/hosts/blob/master/LICENSE) 发布。
- 本项目的.java，README.MD，wiki等资源基于[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)
  这意味着你可以拷贝、并再发行本项目的内容，但是你将必须同样**提供原作者信息以及协议声明**。同时你也**不能将本项目用于商业用途**，
  按照我们狭义的理解（增加附属条款），凡是**任何盈利的活动皆属于商业用途**。
- 请在遵守当地相关法律法规的前提下使用本项目。

![img-source-from-https://github.com/docker/dockercraft](https://github.com/docker/dockercraft/raw/master/docs/img/contribute.png?raw=true)
