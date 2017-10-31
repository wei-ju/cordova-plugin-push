var sPush = function () {}

sPush.prototype.receiveMessage = function (data) {
	cordova.fireDocumentEvent("receivePushMessage",{message:data},false);
}