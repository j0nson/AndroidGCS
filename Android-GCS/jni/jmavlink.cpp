#include "include_v1.0/ardupilotmega/version.h"
#include "include_v1.0/mavlink_types.h"
#include "include_v1.0/ardupilotmega/mavlink.h"

#include <string.h>
#include <jni.h>

//#include <android/log.h>

// Declare each of the known java classes
#include "classDeclarations.h"
jclass interface_class;

// Declare each of the parser functions.
// These convert from a MAVLink C message to the java equivalent.
#include "parseFunctions.h"

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_MAVLink_MAVLink
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_MAVLink_MAVLink_init(JNIEnv * env, jclass){

	// Load each of the known classes. Saves having to do it each and every time.
	#include "classLoader.h"
	// Also prepare the interface class
	interface_class = env->FindClass( "com/MAVLink/Messages/IMAVLinkMessage");

}

/*
 * Class:     com_MAVLink_MAVLink
 * Method:    receivedByte
 * Signature: (B)Lcom/bvcode/MAVLink/IMAVLinkMessage;
 */
JNIEXPORT jobject JNICALL Java_com_MAVLink_MAVLink_receivedByte(JNIEnv * env, jclass objNotUsed, jbyte b){

	mavlink_message_t message;
    mavlink_status_t status;

    unsigned char data = b;
    unsigned int decodeState = mavlink_parse_char(0 /*link->getId()*/, (uint8_t)(data), &message, &status);

    if( decodeState == 1){

    	jobject result;
		//__android_log_print(ANDROID_LOG_DEBUG, "TEST", "Received Msg: %d", message.msgid );

    	switch(message.msgid){
    		// Switch to the correct receive unpack function (defined in parseFunctions.h)
			#include "switchReceive.h"

    	default:
    		result = NULL;

    	}

    	return result;

    }

    return NULL;

}


/*
 * Class:     com_MAVLink_MAVLink
 * Method:    createMessage
 * Signature: (Lcom/bvcode/MAVLink/IMAVLinkMessage;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_MAVLink_MAVLink_createMessage(JNIEnv * env, jclass objNotUsed, jobject obj){

	bool parsed = false;
	mavlink_message_t msg;

	int messageType = env->GetIntField (obj, env->GetFieldID(interface_class, "messageType", "I"));

	switch(messageType){

	#include "packSelection.h"

	}

	//---------------------------------------------------------------
	// Off it goes.
	if(parsed){
		uint8_t buffer[MAVLINK_MAX_PACKET_LEN];

		// Write message into buffer, prepending start sign
		int len = mavlink_msg_to_send_buffer(buffer, &msg);

		// Lets send it onto Java.
		jbyteArray jb = env->NewByteArray(len);

		// Get the elements (you probably have to fetch the length of the array as well
		jbyte* data = env->GetByteArrayElements(jb, NULL);

		for(int i = 0; i < len; i++)
			data[i] = buffer[i];

		// Don't forget to release it
		env->ReleaseByteArrayElements(jb, data, 0);

		return (jb);
	}

	return NULL;

}

#ifdef __cplusplus
}
#endif
