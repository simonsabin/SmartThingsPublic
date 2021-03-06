/*

HTTP Button
Category: Device Handler
Source: [BETA RELEASE] URI Switch -- Device Handler for controlling items via HTTP calls 1
Credit: tguerena, surge919, CSC
v1.1 - changes to enhance the behavior of the button and make it a momentary button
*/
import groovy.json.JsonSlurper

metadata {
definition (name: "HTTP Button", namespace: "sc", author: "SC") {
capability "Switch"	
}

preferences {
	input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
	input("DevicePort", "string", title:"Device Port", description: "Empty assumes port 80.", required: false, displayDuringSetup: true)
	input("DevicePathOn", "string", title:"URL Path", description: "Rest of the URL, include forward slash.", displayDuringSetup: true)
	//input("DevicePathOff", "string", title:"URL Path for OFF", description: "Rest of the URL, include forward slash.", displayDuringSetup: true)
	input(name: "DevicePostGet", type: "enum", title: "POST or GET", options: ["POST","GET"], defaultValue: "POST", required: false, displayDuringSetup: true)
	section() {
		input("HTTPAuth", "bool", title:"Requires User Auth?", description: "Choose if the HTTP requires basic authentication", defaultValue: false, required: true, displayDuringSetup: true)
		input("HTTPUser", "string", title:"HTTP User", description: "Enter your basic username", required: false, displayDuringSetup: true)
		input("HTTPPassword", "string", title:"HTTP Password", description: "Enter your basic password", required: false, displayDuringSetup: true)
	}
}


// simulator metadata
simulator {
}

// UI tile definitions
tiles {
	standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
		state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
		state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
	}
	main "switch"
	details "switch"
}
}

def parse(description) {
    log.debug("parse")
    def msg = parseLanMessage(description)

    def headersAsString = msg.header // => headers as a string
    log.debug("headers-$headersAsString")
    def headerMap = msg.headers      // => headers as a Map
    
    def body = msg.body              // => request body as a string
    log.debug("body-$body")
    def status = msg.status
    log.debug("status-$status")
}

def on() {
push()
}

def off() {
push()
}

def push() {
    log.debug "—Sending command— bob"
    sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
    runCmd(DevicePathOn)
}

def runCmd(String varCommand) {
   /*def host = DeviceIP
    def LocalDevicePort = ''
    if (DevicePort==null) { LocalDevicePort = "80" } else { LocalDevicePort = DevicePort }

    log.debug("LocalDvicePort: $host $LocalDevicePort")
    def userpassascii = "${HTTPUser}:${HTTPPassword}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()

    log.debug "The device id configured is: $device.deviceNetworkId"

    def path = varCommand
    log.debug "path is: $path"
    //log.debug "Uses which method: $DevicePostGet"
    def body = "" 
    //log.debug "body is: $body"

    def headers = [:] 
    headers.put("HOST", "$host:$LocalDevicePort")
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (HTTPAuth) {
        headers.put("Authorization", userpass)
    }
	log.debug "The Header is $headers"
	def method = "POST"
    try {
        if (DevicePostGet.toUpperCase() == "GET") {
            method = "GET"
            }
        }
    catch (Exception e) {
        settings.DevicePostGet = "POST"
        log.debug e
        log.debug "You must not have set the preference for the DevicePOSTGET option"
        }
    log.debug "The method is $method"
    */
    def method = "POST"
//	def method = "GET"
	def path = "/index.php"
    def headers = [:] 
    headers.put("HOST", "192.168.2.120:80")
    headers.put("Content-Type","application/x-www-form-urlencoded")
    def body = "login=admin&pass=84Gv%2661lfiOYiglt&send-login=Sign+in"
    log.debug("before action")
    try {
        def hubAction = new physicalgraph.device.HubAction(
            method: method,
            path: path,
            body: body,
            headers: headers
            )
        log.debug hubAction
        
	log.debug("bfore return")
        return hubAction
    }
    catch (Exception e) {
        log.debug "Hit Exception $e on $hubAction"
    }
	log.debug("Hello after ation")
    //sendEvent
    if (varCommand == "off"){
        sendEvent(name: "switch", value: "off")
        log.debug "Executing OFF"
    } else {
        sendEvent(name: "switch", value: "on")
        log.debug "Executing ON"
    }
}