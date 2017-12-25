#极光
JPUSH_API_KEY="xxx"
#小米(id 与 key 加入前缀mi 因为纯数字情况下metadata会误认为float类型)
XIAOMI_APP_ID="xxx"
XIAOMI_APP_KEY="xxx"
#DDPush
DDPUSH_APP_ID=11111
DDPUSH_SERVER_PORT=11111
DDPUSH_SERVER_ADDR="xxxxx"
#你插件所在地址
PLUGIN_PATH="https://github.com/wei-ju/cordova-plugin-push"
#脚本开始
ionic cordova plugin add $PLUGIN_PATH --variable JPUSH_API_KEY=$JPUSH_API_KEY --variable XIAOMI_APP_ID=$XIAOMI_APP_ID --variable XIAOMI_APP_KEY=$XIAOMI_APP_KEY --variable DDPUSH_APP_ID=$DDPUSH_APP_ID --variable DDPUSH_SERVER_PORT=$DDPUSH_SERVER_PORT --variable DDPUSH_SERVER_ADDR=$DDPUSH_SERVER_ADDR


