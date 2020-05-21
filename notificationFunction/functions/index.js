'use strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification= functions.firestore.document("Users/{blogUserId}/Notifications/{notificationId}").onWrite((change,context) =>{

	const blogUserId=context.params.blogUserId;
	const notificationId=context.params.notificationId;

	return admin.firestore().collection("Users").doc(blogUserId).collection("Notifications").doc(notificationId).get().then(queryResult=>{

		const from_user_id=queryResult.data().fromUser;
		const from_type=queryResult.data().type;

		const from_data= admin.firestore().collection("Users").doc(from_user_id).get();
		const to_data= admin.firestore().collection("Users").doc(blogUserId).get();

		return Promise.all([from_data,to_data]).then(result=>{
			const from_name= result[0].data().name;
			const to_name= result[1].data().name;
			const token_id= result[1].data().token_id;

			
			const payload={
				notification: {
					title: "Notification from: "+ from_name,
				body: from_name + " " +from_type + " your post!",
				icon: "default",
				click_action: "com.example.deeblog.TARGETNOTIFICATION"
				},
				data:{
					type: from_type,
					fromUser: from_user_id
				}
			};
			return admin.messaging().sendToDevice(token_id, payload).then(result=>{

				console.log("Notification Sent");
			});

		});

	});


});