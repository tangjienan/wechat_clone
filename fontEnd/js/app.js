window.app = {
	serverUrl: 'http://192.168.0.6:8080',
	
	imgServerUrl: 'http://192.168.0.11:88/imooc/',
	
	nettyServerUrl: 'ws://192.168.0.6:8088/ws',
	
	showToast: function(msg, type) {
		plus.nativeUI.toast(msg, 
			{icon: "image/" + type + ".png", verticalAlign: "center"})
	},
	
	isNotNull: function(str) {
		if (str != null && str != "" && str != undefined) {
			return true;
		}
		return false;
	},
		
	setUserGlobalInfo: function(user) {
		var userInfoStr = JSON.stringify(user);
		console.log("set new ")
		plus.storage.setItem("userInfo", userInfoStr);
	},
	
	getUserGlobalInfo: function() {
		var userInfoStr = plus.storage.getItem("userInfo");
		return JSON.parse(userInfoStr);
	},
	
	userLogout: function() {
		plus.storage.removeItem("userInfo");
	},
	
	setContactList: function(contactList) {
		var contactListStr = JSON.stringify(contactList);
		plus.storage.setItem("contactList", contactListStr);
	},
	

	getContactList: function() {
		var contactListStr = plus.storage.getItem("contactList");
		
		if (!this.isNotNull(contactListStr)) {
			return [];
		}
		
		return JSON.parse(contactListStr);
	},
	
	getFriendFromContactList: function(friendId) {
		var contactListStr = plus.storage.getItem("contactList");
		
		
		if (this.isNotNull(contactListStr)) {

			var contactList = JSON.parse(contactListStr);
			for (var i = 0 ; i < contactList.length ; i ++) {
				var friend = contactList[i];
				if (friend.friendUserId == friendId) {
					return friend;
					break;
				}
			}
		} else {
			
			return null;
		}
	},
	
	getUserChatHistory: function(myId, friendId) {
		var me = this;
		var chatKey = "chat-" + myId + "-" + friendId;
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if (me.isNotNull(chatHistoryListStr)) {
			chatHistoryList = JSON.parse(chatHistoryListStr);
		} else {
			chatHistoryList = [];
		}
		
		return chatHistoryList;
	},
	deleteUserChatHistory: function(myId, friendId) {
		var chatKey = "chat-" + myId + "-" + friendId;
		plus.storage.removeItem(chatKey);
	},
	saveUserChatSnapshot: function(myId, friendId, msg, isRead) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		
		
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			
			for (var i = 0 ; i < chatSnapshotList.length ; i ++) {
				if (chatSnapshotList[i].friendId == friendId) {
					
					chatSnapshotList.splice(i, 1);
					break;
				}
			}
		} else {
			
			chatSnapshotList = [];
		}
		
		
		var singleMsg = new me.ChatSnapshot(myId, friendId, msg, isRead);
		
		chatSnapshotList.unshift(singleMsg);
		
		plus.storage.setItem(chatKey, JSON.stringify(chatSnapshotList));
	},
	getUserChatSnapshot: function(myId) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
		} else {
			
			chatSnapshotList = [];
		}
		
		return chatSnapshotList;
	},
	deleteUserChatSnapshot: function(myId, friendId) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		
		
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			
			for (var i = 0 ; i < chatSnapshotList.length ; i ++) {
				if (chatSnapshotList[i].friendId == friendId) {
					
					chatSnapshotList.splice(i, 1);
					break;
				}
			}
		} else {
			
			return;
		}
		
		plus.storage.setItem(chatKey, JSON.stringify(chatSnapshotList));
	},
	readUserChatSnapshot: function(myId, friendId) {
		var me = this;
		var chatKey = "chat-snapshot" + myId;
		
		var chatSnapshotListStr = plus.storage.getItem(chatKey);
		var chatSnapshotList;
		if (me.isNotNull(chatSnapshotListStr)) {
			
			chatSnapshotList = JSON.parse(chatSnapshotListStr);
			
			for (var i = 0 ; i < chatSnapshotList.length ; i ++) {
				var item = chatSnapshotList[i];
				if (item.friendId == friendId) {
					item.isRead = true;		
					chatSnapshotList.splice(i, 1, item);	
					break;
				}
			}
			
			plus.storage.setItem(chatKey, JSON.stringify(chatSnapshotList));
		} else {
			
			return;
		}
	},
	
	// flag 1: I send, 2 other send
	saveUserChatHistory: function(myId, friendId, msg, flag) {
		var me = this;
		
		// UID key for each chat history
		var chatKey = "chat-" + myId + "-" + friendId;
		
		// If there is chat history before
		var chatHistoryListStr = plus.storage.getItem(chatKey);
		var chatHistoryList;
		if (me.isNotNull(chatHistoryListStr)) {
			
			chatHistoryList = JSON.parse(chatHistoryListStr);
		} else {
			
			chatHistoryList = [];
		}
		
		
		var singleMsg = new me.ChatHistory(myId, friendId, msg, flag);
		
		
		chatHistoryList.push(singleMsg);
		
		plus.storage.setItem(chatKey, JSON.stringify(chatHistoryList));
	},
	
//type enum
	CONNECT: 1, 	
	CHAT: 2, 		
	SIGNED: 3, 		
	KEEPALIVE: 4, 	
	PULL_FRIEND:5,	
	
	
	ChatMsg: function(senderId, receiverId, msg, msgId){
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.msg = msg;
		this.msgId = msgId;
	},
	
	
	DataContent: function(action, chatMsg, extand){
		this.action = action;
		this.chatMsg = chatMsg;
		this.extand = extand;
	},
	

	ChatHistory: function(myId, friendId, msg, flag){
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.flag = flag;
	},
	

	ChatSnapshot: function(myId, friendId, msg, isRead){
		this.myId = myId;
		this.friendId = friendId;
		this.msg = msg;
		this.isRead = isRead;
	}
	
	
}