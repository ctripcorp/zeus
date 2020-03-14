

<!-- @import "[TOC]" {cmd="toc" depthFrom=1 depthTo=6 orderedList=false} -->

<!-- code_chunk_output -->

- [请求状态码](#请求状态码)
  - [499错误码](#499错误码)
  - [502错误码](#502错误码)
  - [504错误码](#504错误码)
  - [413、414错误码](#413-414错误码)
  - [NoHttpResponse&ConnectionReset](#nohttpresponseconnectionreset)

<!-- /code_chunk_output -->

# 请求状态码
## 499错误码
简介：499 Code是客户端在请求SLB后，在获取到Response前，客户端断开了连接。此时由于连接已经断开，SLB无法将Response返回给客户端，SLB此时不管后端服务返回码是多少均记录为ErrorCode 499。并且客户端是服务接收到499状态码的，仅是SLB自己记录的状态码。499, client has closed connection

499错误码并不返回给客户端，因为客户端已经断开连接，SLB会将499记录的AccessLog中，SLB的监控日志中可以看到499. 客户端通常出现报错：ReadTimeout，SocketTimeout类型的异常。

## 502错误码
简介：502 Code是客户端在请求SLB后，SLB收到了Resquest，并向后端服务转发。如果无法获取到可用后端服务或者请求发送给后端服务后，后端服务未能成功返回Response。此时SLB会返回502 Code。

502可能是哪里返回的：

SLB返回502；出现场景：SLB无法获取到可用后端服务或者请求发送给后端服务后，后端服务未能成功返回Response；比如：应用服务器健康监测全部拉出，或者应用服务器宕机、网络故障不通、后端服务拒绝等。
后端服务返回502；出现场景：502也是一种ErrorCode，也可以是后端服务返回的。一般出现在代理服务返回该Code。比如：NodeJS的应用服务，由于该类服务本机会搭建Nginx，如果这里出现故障可能返回502、后端服务为代理服务，后端服务再向后转发时也可能遇到同SLB无法获取到可用后端服务的类似问题、后端服务为k8s的service类型，即使用了k8s的负载均衡Service或者搭建了nginx实现k8s的负载均衡，同样可能出现类似情况。

## 504错误码
简介：504Code是客户端在请求SLB，SLB成功向后端服务转发请求，然而在SLB的超时阈值内并未返回Response，SLB认为后端服务已经超时并返回504 Gateway Timeout

502可能是哪里返回的：

504Code为SLB返回的。后端服务的响应码可能是任意的状态码
由于已经达到SLB的超时阈值，SLB将不再等待后端服务的返回，直接返回504给客户端。所以后端服务的Code可能不能被记录到。应用服务器的WebLog应该可以查看到。
## 413、414错误码
SLB对请求的Header和URI长度默认有一定限制。超过SLB配置的超时时间时，SLB会返回400 (Bad Request) 或者414 (Request-URI Too Large)错误码。

这里当超过阈值时414 400 均为SLB返回给客户端的状态码，但是如果Header及URI可能没有超过SLB的阈值，SLB继续进行了转发，而后端服务也可能返回了400或者414。所以414,400 可能是SLB返回，也可能是后端服务返回，通过SLB的UpstreamStatus字段区分。

## NoHttpResponse&ConnectionReset
触发原因:
经过我们的排查，这两种异常很大概率是由于支持目标服务的SLB集群进行reload操作所导致的。

解决方案:
增加重试次数，降低重试门槛，向Apache HttpClient中的默认重试机制靠近。相信通过这样的调整，虽然可能无法完全消灭这两种异常，但也可以将它的数量降低到可以接受的程度。