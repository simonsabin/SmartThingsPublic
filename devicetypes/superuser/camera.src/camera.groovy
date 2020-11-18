/**
 *	D-Link DCS-932L v1.0.0
 *  Modified from Generic Camera Device v1.0.07102014
 *
 *  Copyright 2014 patrick@patrickstuart.com
 *  Modified 2015 blebson
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "camera", author: "sms") {
		capability "DoorControl"
		capability "Switch"
        capability "Refresh"
		attribute "hubactionMode", "string"
	}

    preferences {
    input("CameraIP", "string", title:"Camera IP Address", description: "Please enter your camera's IP Address", required: true, displayDuringSetup: true)
    input("CameraPort", "string", title:"Camera Port", description: "Please enter your camera's Port", defaultValue: 80 , required: true, displayDuringSetup: true)
    input("CameraUser", "string", title:"Camera User", description: "Please enter your camera's username", required: false, displayDuringSetup: true)
    input("CameraPassword", "string", title:"Camera Password", description: "Please enter your camera's password", required: false, displayDuringSetup: true)
	}
    
	simulator {
    
	}

    tiles {
    	   standardTile("status", "device.status", width: 2, height: 2) {
            state("closed", label:'${name}', icon:"st.doors.garage.garage-closed", action: "actuate", backgroundColor:"#79b821", nextState:"opening")
            state("open", label:'${name}', icon:"st.doors.garage.garage-open", action: "actuate", backgroundColor:"#ffa81e", nextState:"closing")
            state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
            state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
        }
    }
}

def initialize() {
    login()
    refresh()
}

def getwebtoken(){
}
def open(door)
{
	log.debug "webtoken = ${state.webtoken}"
    call "GET","/isg/opendoor.php?numdoor=$door&status0"
}
def close(door)
{
	log.debug "webtoken = ${state.webtoken}"
    call "GET","/isg/opendoor.php?numdoor=$door&status1"
}

def open(){
	log.debug "webtoken = ${state.webtoken}"
   open 3
}
def close(){
	close 3
}

//https://community.smartthings.com/t/device-attribute-syntax-from-within-a-device-type/9388/2

def parse(String description) {

	try {
        def map = [:]
        def retResult = []
                log.debug "a5"

       // def descMap = parseDescriptionAsMap(description.headers)
        log.debug "a4"
        def msg = parseLanMessage(description)
                log.debug "a3"

        log.debug "status ${msg.status}"
                log.debug "a2"

        log.debug "data ${msg.headers}"
        if (msg.headers.get("set-cookie")){
        	def cookiefind = msg.headers.get("set-cookie").split(";")[0]
			state.cookie =cookiefind
            log.debug "cookie ${cookiefind}"
        }
        log.debug "a1"
        log.debug "body ${msg.body}"

        if (msg.body.substring(0,2)=="{\""){
        	def slurp = new groovy.json.JsonSlurper()
            def doors = slurp.parseText(msg.body)
        	log.debug ("door 1 ${doors.get('1')}")
        	log.debug ("door 2 ${doors.get("2")}")
        	log.debug ("door 3 ${doors.get("3")}")
            
        }
        else{
                log.debug "getting token"
                try{
                //def pattern = Pattern.compile("webtoken.*value=\"(?<webtoken>.*)\"")
                def match = msg.body =~ "\"webtoken\".*value=\"(?<webtoken>.*)\""

               state.webtoken= match[0][1]
               }
               catch (e)
               {
               log.debug "bugger2"

               }
            }
        }
        catch (e){
	log.debug "bugger"
    		log.debug e
        }
    }



def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}


private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    log.debug hexport
    return hexport
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}


private String convertHexToIP(hex) {
	log.debug("Convert hex to ip: $hex") 
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
    def parts = device.deviceNetworkId.split(":")
    log.debug device.deviceNetworkId
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    return ip + ":" + port
}

def refresh(){
 call "GET","/isg/statusDoorAll.php?status1=0&status2=undefined&status3=undefined&access=1"
 	
 }
 def login(){
 
	log.debug "Refresh"
	def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    //def path = "login.php"
    def path = "/index.php"
    log.debug "path is: $path"
    
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
    headers.put("Content-Type","application/x-www-form-urlencoded")
    def body = "login=admin&pass=84Gv%2661lfiOYiglt&send-login=Sign+in"
    log.debug "The Header is $headers"
   
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: "POST",
    	path: path,
    	headers: headers,
        body:body
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
  
  
}

def call(method,path){

	log.debug "Refresh"
	def host = CameraIP 
    def hosthex = convertIPtoHex(host)
    def porthex = convertPortToHex(CameraPort)
    device.deviceNetworkId = "$hosthex:$porthex" 
    
    log.debug "The device id configured is: $device.deviceNetworkId"
    log.debug "path is: $path"
    
    def headers = [:] 
    headers.put("HOST", "$host:$CameraPort")
    headers.put("Cookie",state.cookie)
    headers.put("Accept","application/json")
    log.debug "The Header is $headers"
   def fullpath = "$path&login=admin&webtoken=${state.webtoken}"
    
  try {
    def hubAction = new physicalgraph.device.HubAction(
    	method: method,
    	path: fullpath,
    	headers: headers,
        body:body
        )
        	
   
    log.debug hubAction
    return hubAction
    
    }
    catch (Exception e) {
    	log.debug "Hit Exception $e on $hubAction"
    }
}