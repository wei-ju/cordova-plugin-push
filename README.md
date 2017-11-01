当前项目整合了小米，华为，极光，DDPush推送，使用时需将项目中的add_push_plugin.sh中的变量重新配置，并放于所要加载插件的ionic项目的根目录下执行。

以下为需要修改的插件所需变量：

```bash
>#极光
>JPUSH_API_KEY（String）

>#小米
>XIAOMI_APP_ID (String）
>XIAOMI_APP_KEY (String）

>#DDPush
>DDPUSH_APP_ID (Integer）
>DDPUSH_SERVER_PORT (Integer）
>DDPUSH_SERVER_ADDR (String）

>#你插件项目所在地址
>PLUGIN_PATH (String）

```
### 若想对插件进行修改可参照cordova官方提供的文档
[http://cordova.apache.org/docs/en/latest/plugin_ref/spec.html](http://cordova.apache.org/docs/en/latest/plugin_ref/spec.html)
