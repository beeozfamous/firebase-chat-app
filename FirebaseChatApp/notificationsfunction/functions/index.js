'use strict'
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change, context) => {

    const user_id = context.params.user_id;
    console.log('User_id:', user_id);
    const notification__id = context.params.notification_id;

    if (!change.after.exists()) {
        return console.log('Anotification has been deleted from database : ', notification_id);
    }



    const fromUser = admin.database().ref(`/notifications/${user_id}/${notification__id}`).once('value');
    return fromUser.then(fromUserResult => {
        const from_user_id = fromUserResult.val().from;

        const userQuery = admin.database().ref(`users/${from_user_id}/name`).once('value');
        return userQuery.then(userResult => {

            const userName = userResult.val();


            console.log('You have new notification from  : ', from_user_id)
            const deviceToken = admin.database().ref(`/users/${user_id}/device_token`).once('value');

            return deviceToken.then(result => {

                const token_id = result.val();

                const payLoad = {
                    notification: {
                        title: "Friend Request",
                        body: `You got a friend request from ${userName} !!`,
                        icon: "default",
                        click_action: "com.project.helloworst.firebasechatapp_TARGET_NOTIFICATION"
                    },
                    data: {
                        UserID: from_user_id
                    }
                };

                return admin.messaging().sendToDevice(token_id, payLoad).then(response => {

                    console.log('This was the notification fearture');

                })

            })
        })

    });
    console.log('Notify_id:', notification__id)
    return null;
});

