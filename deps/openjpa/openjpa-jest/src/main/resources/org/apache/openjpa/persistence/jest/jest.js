/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

dojo.require("dijit.form.Button");
dojo.require("dijit.TitlePane");
dojo.require("dojox.xml.parser");
dojo.require("dijit.Dialog");


/** -----------------------------------------------------------------------------------------
 *                        Navigation functions
 *
 * Available commands appear on left menu. Clicking on any command makes a corresponding 
 * highlighted section to appear at a fixed position. These command windows are identified 
 * and these identifiers are used to make only one of the section to be visible while 
 * hiding the rest.
 *
 * The jest.html document will use these same identifiers in their command section. 
 * ------------------------------------------------------------------------------------------
 *
 * The identifiers of every element to be shown or hidden.
 * --------------------------------------------------------------------------------------- */ 

var menuIds = new Array('home', 'deploy', 'find', 'query', 'domain', 'properties');

/**
 * Opening a menu implies clearing the canvas, hiding the current menu item and making
 * the given menu identifier visible.
 * 
 * @param id menu section identifier 
 */
function openMenu(/*HTML element id*/id) {
	clearElement('canvas');
	switchId(id, menuIds);
    document.location = "#top";
}
/**
 * Show the division identified by the given id, and hide all others
 */
function switchId(/*string*/id, /*string[]*/highlightedSections) {	
    for (var i=0; i < highlightedSections.length; i++){
    	var section = document.getElementById(highlightedSections[i]);
    	if (section != null) {
             section.style.display = 'none';
    	}
    }
    var div  = document.getElementById(id);
    if (div != null) {
        div.style.display = 'block';
        div.focus();
    }
}

/** -----------------------------------------------------------------------------------------
 *    Generic Command handling functions
 *    
 *  All available JEST commands are enumerated by their qualifiers and arguments.
 *  
 *  Specification of a JEST Command requires the following:
 *     a) a name
 *     b) zero or more Qualifiers. Qualifiers are not ordered. All Qualifiers are optional. 
 *     c) zero or more Arguments. Arguments are ordered. Some Arguments can be mandatory. 
 *     
 *  Command, Qualifier and Argument are 'objects' -- in a curious JavaScript sense. They
 *  are responsible to harvest their state value from the HTML element such as a input
 *  text box or a check box etc. The aim of harvesting the values is to construct a 
 *  relative URI that can be passed to the server via a HTTP get request.
 *  
 *  jest.html attaches change event handlers to all input elements and the on change
 *  event handler updates the URI.
 *  
 *  The complexity is added because some commands may take variable number of arguments.
 *  Hence the input text boxes to enter arbitrary number of key-value pair can be created 
 *  or removed by the user.
 *  
 *  A lot of implicit naming convention for the element identifiers are used in this 
 *  script. These naming conventions are documented in jest.html.     
 * --------------------------------------------------------------------------------------- */
var findCommand = new Command('find', 
		new Array( // 
		   new Qualifier("plan",        "plan",        false),
		   new Qualifier("format",      "format",      false),
		   new Qualifier("ignoreCache", "ignoreCache", true)
		), 
		new Array( // Order of arguments is significant
		   new Argument("type", "type", true),
		   new Argument("pk",   null,   true)
		));

var queryCommand = new Command('query', 
		new Array( // Qualifiers are not ordered
		   new Qualifier("plan",        "plan",        false),
		   new Qualifier("format",      "format",      false),
		   new Qualifier("single",      "single",      true),
		   new Qualifier("named",       "named",       true),
		   new Qualifier("ignoreCache", "ignoreCache", true)
		), 
		new Array( // Order of arguments is significant
		   new Argument("q", "q", true)
		));

var domainCommand = new Command('domain', new Array(), new Array());

var propertiesCommand = new Command('properties', new Array(), new Array());

var commands = new Array(findCommand, queryCommand, domainCommand, propertiesCommand);

/** -----------------------------------------------------------------------------------------
 * Creates a relative URI for the given commandName by reading the content of the given HTML 
 * element and its children.
 * The URI is written to the HTML anchor element identified by {commandName} + '.uri'.
 * 
 * @param commandName name of the command. All source HTML element are identified by this 
 * command name as prefix. 
 * --------------------------------------------------------------------------------------- */
function toURI(commandName) {
	var command = null;
	switch (commandName) {
	  case 'find'       : command = findCommand;        break;
	  case 'query'      : command = queryCommand;       break;
	  case 'domain'     : command = domainCommand;      break;
	  case 'properties' : command = propertiesCommand;  break;
	}
	if (command != null)
		command.toURI();
}

/** -----------------------------------------------------------------------------------------
 * Adds columns to the given row for entering a variable argument key-value pair.
 * A remove button is created as well to remove the row.
 * A new row is created to invoke this function again to add another new row. 
 * 
 * @param rowIdPrefix a string such as query.vararg or find.vararg
 * @param index a integer appended to the prefix to identify the row such as query.vararg.3
 * @param message the label on the new button 
 * --------------------------------------------------------------------------------------- */
function addVarArgRow(rowIdPrefix, index, message) {
	var rowId = rowIdPrefix + '.' + index;
	var row = document.getElementById(rowId);
	clearElement(rowId);
	
	// New input column for parameter name. Element id is rowId + '.key'
	var argNameColumn  = document.createElement('td');
	var argNameInput   = document.createElement('input');
	argNameInput.setAttribute('type', 'text');
	argNameInput.setAttribute('id', rowId + '.key');
	argNameInput.setAttribute('onblur', 'javascript:toURI("' + rowIdPrefix.split('.')[0] + '");');
	argNameColumn.appendChild(argNameInput);
	
	// New input column for parameter value. Element id is rowId + '.value'
	var argValueColumn = document.createElement('td');
	var argValueInput  = document.createElement('input');
	argValueInput.setAttribute('type', 'text');
	argValueInput.setAttribute('id', rowId + '.value');
	argValueInput.setAttribute('onblur', 'javascript:toURI("' + rowIdPrefix.split('.')[0] + '");');
	argValueColumn.appendChild(argValueInput);
	
	// New column for remove button. Will remove this row.
	var removeColumn   = document.createElement('td');
	var removeColumnButton  = document.createElement('button');
	removeColumnButton.innerHTML = 'Remove';
	removeColumnButton.setAttribute('onclick', 'javascript:removeVarArgRow("' + rowId + '");');
	removeColumn.appendChild(removeColumnButton);
	
	// Empty column as the first column
	var emptyColumn = document.createElement('td');
	emptyColumn.appendChild(document.createTextNode("Key-Value pair"));
	
	// Add the empty column, two input columns and remove button to the current row
	row.appendChild(emptyColumn); 
	row.appendChild(argNameColumn);
	row.appendChild(argValueColumn);
	row.appendChild(removeColumn);
	
	// create a new row with a single column to add another parameter.
	// This new row looks similar to the original state of the modified column
	var newIndex = index + 1;
	var newRowId = rowIdPrefix + '.' + newIndex;
	var newRow = document.createElement('tr');
	newRow.setAttribute('id', newRowId);
	var newColumn      = document.createElement('td');
	var addColumnButton = document.createElement('button');
	addColumnButton.innerHTML = message;
	addColumnButton.setAttribute('onclick', 'javascript:addVarArgRow("' + rowIdPrefix + '",' + newIndex + ',"'
			+ message + '");');
	newColumn.appendChild(addColumnButton);
	
	newRow.appendChild(newColumn);
	row.parentNode.appendChild(newRow);
}

/** -----------------------------------------------------------------------------------------
 * Removes a variable argument row.
 * The URI is updated.
 * 
 * @param rowId the identifier of the row to be removed. The identifier follows the
 * naming convention of the variable argument row i.e. {commandName}.varargs.{n}
 * --------------------------------------------------------------------------------------- */
function removeVarArgRow(rowId) {
	var row = document.getElementById(rowId);
	row.parentNode.removeChild(row);
	toURI(rowId.split('.')[0]);
}

/** -----------------------------------------------------------------------------------------
 * Definition of Command as a JavScript object.
 * 
 * @param name name of the command. Used to identify the command, or identify input elements.
 * @param qualifiers zero or more Qualifier objects 
 * @param arguments zero or more Argument objects
 * 
 * 
 * --------------------------------------------------------------------------------------- */
function Command(name, qualifiers, arguments) {
	this.name       = name;
	this.qualifiers = qualifiers;
	this.arguments  = arguments;
	this.toURI      = Command_toURI;
}

/** -----------------------------------------------------------------------------------------
 * Harvests the input HTML elements for a commands qualifiers and arguments and builds up
 * a URI.
 * Uses several naming convention that are documented in jest.html to identify the input
 * elements.
 * The naming of the function and its capitalization follows JavaScript convention for it
 * to behave as a faux object method.
 * 
 * @returns a string form of URI
 * --------------------------------------------------------------------------------------- */
function Command_toURI() {
	var uri = this.name; // command name is same as URI name -- need not be
	var iformat = 'xml';  // default response format
	for (var i = 0; i < this.qualifiers.length; i++) {
		var id = this.name + '.' + this.qualifiers[i].name;
		var inode = document.getElementById(id);
		var path = this.qualifiers[i].toURI(inode);
		if (path != null) {
			uri = uri.concat('/').concat(path);
			if (this.qualifiers[i].key == 'format') {
				iformat = getNodeValue(inode);
			}
		}
	}
	var args = "";
	var invalid = null;
	for (var i = 0; i < this.arguments.length; i++) {
		var id = this.name + '.' + this.arguments[i].name;
		var inode = document.getElementById(id);
		var arg = this.arguments[i].toURI(inode);
		if (arg != null) {
			args = args.concat(args.length == 0 ? '' : '&').concat(arg);
		} else if (this.arguments[i].mandatory) {
			invalid = 'Missing mandatory ' + this.arguments[i].name + ' argument';
		}
	}

	// Variable argument processing
	var children = document.getElementById(this.name + '.command').getElementsByTagName('tr');
	for (var i = 0; i < children.length; i++) {
		var child = children[i];
		if (isVarArgRow(child, this.name)) {
			var varargRow = child;
			var pair  = varargRow.getElementsByTagName('input');
			var key   = getNodeValue(pair[0]);
			var value = getNodeValue(pair[1]);
			if (key != null && value != null) {
				args = args.concat(args.length == 0 ? '' : '&').concat(key).concat('=').concat(escape(value));
			}
		}
	}
	if (args.length > 0) {
		uri = uri.concat('?').concat(args);
	}
	
	// update the command URI element
	console.log("New URI is " + uri);
	var uriNode  = document.getElementById(this.name + ".uri");
	var uriCtrl  = document.getElementById(this.name + ".execute");
	if (invalid == null) {
		uriNode.setAttribute('class', 'url');
		uriNode.innerHTML = uri;
		var contentType = getContentTypeForCommand(this.name);
		uriCtrl.setAttribute('onclick', 
				'javascript:render("' 
				   + uri 
				   + '", "canvas"' + ',"' 
				   + contentType   + '","' 
				   + iformat + '");');
		uriCtrl.style.display = 'inline';
	} else {
		uriNode.setAttribute('class', 'url-invalid');
		uriNode.innerHTML = uri + ' (' + invalid + ')';
		uriCtrl.style.display = 'none';
		uriCtrl.removeAttribute('onclick');
	}
	return uri;
}

function getContentTypeForCommand(/*string*/ commandName) {
	if (commandName == 'find' || commandName == 'query') return 'instances';
	if (commandName == 'domain') return 'domain';
	if (commandName == 'properties') return 'properties';
	
}

/** -----------------------------------------------------------------------------------------
 *  Definition of Qualifier JavaScript object.
 *  
 *  A qualifier decorates a Command. For example, a query command can be decorated with 
 *  'single' qualifier to return a single result. A 'plan' qualifier can decorate a find or 
 *  query command to use a named fetch plan etc. 
 *  A qualifier is encoded in the path segment of JEST URI followed by the
 *  command name e.g. /query/single or /find/plan=myFetchPlan etc.
 * 
 * 
 * @param name a name when prefixed by the name of the command identifies the HTML element 
 * that carries the value of this qualifier.
 * @param key  the identifier for this qualifier used in the JEST URI
 * @param isBoolean is this qualifier carries a boolean value?
 * 
 * @returns {Qualifier}
 * -------------------------------------------------------------------------------------- */
function Qualifier(name, key, isBoolean) {
	this.name = name;
	this.key  = key;
	this.isBoolean = isBoolean;
	this.toURI = Qualifier_toURI;
}

/** -----------------------------------------------------------------------------------------
 *  Generates a string for this qualifier to appear in the command URI.
 *  
 *  A qualifier is translated to a URI fragment as a key=value pair. A boolean
 *  qualifier is translated to a URI fragment only if the corresponding HTML
 *  element is checked. And even then, only the key is sufficient.
 * 
 * @returns a string
 * --------------------------------------------------------------------------------------- */
function Qualifier_toURI(inode) {
	var value = getNodeValue(inode);
	if (isEmpty(value) || (this.isBoolean && !inode.checked)) { 
		return null;
	}
	if (this.isBoolean) {
		return this.key + (value == 'true' ? '' : '=' + value);
	}
	return this.key + '=' + value;
}

/** -----------------------------------------------------------------------------------------
 *  Definition of Argument JavaScript object.
 *  
 *  An argument for a command. Some argument can be mandatory. <br>
 *  Each argument is encoded as key=value pair in JEST URI in query parameters
 *  separated by '&' character.
 * 
 * @param name a name when prefixed by the name of the command identifies the HTML element 
 * that carries the value of this argument.
 * @param key the identifier for this argument used in the JEST URI
 * @param mandatory is this argument mandatory?
 * 
 * @returns {Argument}
 * -------------------------------------------------------------------------------------- */
function Argument(name, key, mandatory) {
	this.name = name;
	this.key  = key;
	this.mandatory = mandatory;
	this.toURI = Argument_toURI;
}

/** -----------------------------------------------------------------------------------------
 *  Generates a string for this argument to appear in the command URI.
 *  
 *  An argument is translated to a URI fragment as a key=value pair. 
 * 
 * @returns a string
 * --------------------------------------------------------------------------------------- */
function Argument_toURI(inode) {
	var value = getNodeValue(inode);
	if (isEmpty(value))
		return null;
	if (this.key == null) {
		return value;
	} else {
		return this.key + '=' + value;
	}
}

/**  ----------------------------------------------------------------------------------------
 *      Utility functions
 *   ------------------------------------------------------------------------------------- */
/**
 * Trims a String.
 */
String.prototype.trim = function () {
    return this.replace(/^\s*/, "").replace(/\s*$/, "");
};

/**
 * Affirms if the given string appears at the start of this string.
 */
String.prototype.startsWith = function(s) {
	return this.indexOf(s, 0) == 0;
};

/**
 * Affirms if the given string is null or zero-length or trimmed to zero-length.
 * 
 * @param str a string to test for 'emptiness'
 * @returns {Boolean}
 */
function isEmpty(str) {
	return str == null || str.length == 0 || str.trim().length == 0;
}

/**
 * Gets the string value of the given node.
 * 
 * @param inode a HTML element
 * @returns null if given node is null or its value is an empty string. 
 *          Otherwise, trimmed string.
 */
function getNodeValue(inode) {
	if (inode == null) {
		return null;
	}
	if (isEmpty(inode.value)) {
		return null;
	} else {
		return inode.value.trim();
	}
}

/**
 * Affirms if the given HTML row element represents a variable argument row
 * for the given commandName.
 * @param row a HTML row element
 * @param commandName name of a command 
 * 
 * @returns true if row identifer starts with commandName + '.vararg.'
 */
function isVarArgRow(row, commandName) {
	return row != null && row.nodeName != '#text' 
		&& row.hasAttribute("id") 
		&& row.getAttribute("id").startsWith(commandName + '.vararg.');
}

/**
 * Removes all children of the given element.
 */
function clearElement(/* HTML element id */ id) {
	var element = dojo.byId(id);
	if (element == null) return;
	while (element.childNodes.length > 0) {
		element.removeChild(element.firstChild);
	}
}

/**
 * Prints and alerts with the given message string.
 * @param message a warning message
 */
function warn(/*string*/message) {
	console.log(message);
	alert(message);
}

/** -----------------------------------------------------------------------------------------
 *     Rendering functions
 *     
 *  Several rendering functions to display server response in the canvas section.
 *  The server responds in following formats :
 *      1a) XML 
 *      1b) JSON 
 *  The response can be rendered in following display modes :
 *      2a) raw XML text
 *      2b) HTML table
 *      2c) Dojo Widgets
 *      2d) JSON as text
 *  The content can be one of the following
 *      3a) instances from find() or query() command
 *      3b) domain model from domain() command
 *      3c) configuration properties from properties() command
 *      3d) error stack trace
 *      
 *  Thus there are 2x4x4 = 32 possible combinations. However, response format for
 *  certain content type is fixed e.g. server always sends domain/properties/error 
 *  stack trace in XML format. Moreover certain content-response format-display mode
 *  combinations are not supported. The following matrix describes the supported 
 *  display modes for content-response format combinations.
 *  [y] : supported
 *  [x] : not supported
 *  n/a : not available
 *  --------------------------------------------------------------------------------
 *  Response                              Content
 *  --------------------------------------------------------------------------------
 *               instances         domain            properties            error
 *  --------------------------------------------------------------------------------               
 *  XML          [y] XML text      [y] XML text      [y] XML text      [x] XML text
 *               [y] HTML          [y] HTML          [y] HTML          [y] HTML 
 *               [y] Dojo Widgets  [y] Dojo Widgets  [x] Dojo Widgets  [x] Dojo Widgets
 *               [x] JSON          [x] JSON          [x] JSON          [x] JSON
 *                  
 *  JSON         [x] XML text      n/a               n/a               n/a
 *               [x] HTML Table    n/a               n/a               [y] HTML
 *               [x] Dojo Widgets  n/a               n/a               n/a
 *               [y] JSON          n/a               n/a               n/a
 *  ---------------------------------------------------------------------------------
 *  The above matrix shows that there are 10 supported combinations.    
 *   ------------------------------------------------------------------------------------- */
var supportedResponseFormats = new Array('xml', 'json');
var supportedContentTypes    = new Array('instances', 'domain', 'properties', 'error');
var renderingCombo = new Array(
		                                                              
/*XML*/ new Array(new Array('xml', 'dojo', 'html'), // instances
		          new Array('xml', 'dojo', 'html'), // domain
		          new Array('xml','html'),          // properties
		          new Array('html')),               // error
/*JSON*/new Array(new Array('json'),                // instances
		          new Array('xml', 'dojo', 'html'), // domain
		          new Array('xml', 'html'),         // properties
		          new Array('html')));              // error 
/**
 * Gets the ordinal index of the given key in the given array
 * @param array an array of enumerated strings.
 * @param key a key to search for.
 * 
 * @returns {Number} 0-based index of the key in the array.
 */
function getOrdinal(/*Array*/array, /*string*/key) {
	for (var i = 0; i < array.length; i++) {
		if (key == array[i]) return i;
	}
	console.log(key + " is not a valid enum in " + array);
	return 0;
}
/**
 * Gets ordinal number for enumerated response format.
 * 
 * @param iformat response format. one of 'xml', 'json'
 * 
 * @returns {Number} ordinal number 0 for 'xml', 
 */
function getOrdinalResponseFormat(/*enum*/iformat) {
	return getOrdinal(supportedResponseFormats, iformat);
}

/**
 * Gets ordinal number of the enumerated content types.
 * @param contentType type of content. One of 'instances', 'domain', 'properties', 'error'
 * @returns
 */
function getOrdinalContentType(/*enum*/contentType) {
	return getOrdinal(supportedContentTypes, contentType);
}

/**
 * Gets the array of enumerated strings of display format for the given response format and content type.
 * @param iformat
 * @param contentType
 * @returns
 */
function getSupportedDisplayModes(/*enum*/iformat,/*enum*/contentType) {
	var displayModes = renderingCombo[getOrdinalResponseFormat(iformat)][getOrdinalContentType(contentType)];
	if (displayModes == null) {
		warn("No display format for response format [" + iformat + "] and content type [" + contentType + "]");
	}
	return displayModes;
} 

/**
 * Render the response from the given URI on to the given HTML element identified by targetId.
 * 
 * The URI is requested from server in an asynchronous call. Then the server response is rendered
 * in all supported display format but only the given display format is made visible. 
 *  
 * @param uri the request URI
 * @param targetId identifier of the HTML element that will display the data
 * @param contentType type of the content, one of 'instances', 'domain', 'properties', 'error'
 * @param iformat format of the server response, one of 'xml' or 'json'
 * @param oformat format for display, one of 'xml', 'json', 'dojo', 'html'
 * 
 * The combination of iformat-contentType-oformat must be compatiable as described in above matrix.
 * 
 * @returns {Boolean} to prevent default event propagation
 */
function render(/* string */ uri, /* id */ targetId, /* enum */contentType, /* enum */iformat) {
    var targetNode = dojo.byId(targetId);
    clearElement(targetId);

    //The parameters to pass to xhrGet, the url, how to handle it, and the callbacks.
    var xhrArgs = {
        url: uri,
        handleAs: (iformat == 'json' && contentType == 'instances') ? 'json' : 'xml',
        preventCache: contentType == 'instances',
        timeout : 1000,
        load: function(data, ioargs) {
        	if (ioargs.xhr.status == 200) { // HTTP OK
	    		var newDivs = null;
	        	if (iformat == 'json') {
	        		newDivs = renderJSONResponse(data, contentType);
	        	} else {
	        		newDivs = renderXMLResponse(data, contentType);
	        	} 
	    		var displayModes = getSupportedDisplayModes(iformat, contentType);
	        	targetNode.appendChild(createDisplayModeControl(displayModes));
	        	for (var i = 0; i < newDivs.length; i++) {
	        		targetNode.appendChild(newDivs[i]);
	        	}
        	} else {
            	var errorDiv = renderErrorFromXMLAsHTML(data, ioargs);
            	targetNode.appendChild(errorDiv);
        	}
        },
        error: function(error, ioargs) {
        	var errorDiv = renderErrorFromXMLAsHTML(ioargs.xhr.responseXML, ioargs);
        	targetNode.appendChild(errorDiv);
        }
    };

    //Call the asynchronous xhrGet
    var deferred = dojo.xhrGet(xhrArgs);
    return false;
}

/**
 * Creates a table with radio buttons for supported display modes.
 * 
 * @param displayModes name of supported display modes.
 * 
 * @returns an unattached HTML table
 */
function createDisplayModeControl(displayModes) {
	var displayMode = document.createElement("table");
	displayMode.style.width = "100%";
	
	var tr = document.createElement("tr");
	displayMode.appendChild(tr);
	// append columns. 0-th an dfirst columns are descriptive texts. 
	var caption = document.createElement("th");
	caption.style.width = (100 - displayModes.length*12) + '%';
	caption.appendChild(document.createTextNode("JEST Response"));
	tr.appendChild(caption);

    for (var i = 0; i < displayModes.length; i++) {
    	var mode = displayModes[i];
    	var modeColumn = document.createElement("th");
    	modeColumn.style.width = "12%";
    	tr.appendChild(modeColumn);
        
    	var radio = document.createElement("input");
        radio.setAttribute("type", "radio");
        radio.setAttribute("value", mode);
        radio.setAttribute("name", "display.mode");
        if (i == 0) radio.setAttribute("checked", "checked");
       	radio.setAttribute('onchange', createModeSwitchFunction(mode, displayModes));
       	
       	modeColumn.appendChild(radio);
       	modeColumn.appendChild(document.createTextNode(mode.toUpperCase()));
    }
	
	return displayMode;
}

/**
 * Creates a string for javascript function call to switch between display modes
 * @param visible the visible display mode
 * @param all available display modes
 * @returns {String} a event handler function string 
 */
function createModeSwitchFunction(/* string */ visible, /* string[] */ all) {
	var array = '';
	for (var i = 0; i < all.length; i++) {
		if (all[i] != visible) {
			array = array + (array.length == 0 ? '' : ', ') + '"display.mode.' + all[i] + '"';
		}
	}
	return 'javascript:switchId("display.mode.' + visible+ '", [' + array + '])';
}

/**
 * The big switch for rendering all content types received as XML DOM.
 * Finds out the supported display format for given content type and renders each display format
 * in separate divs. The div corresponding to the given display format is made visible, and others
 * are hidden. None of the divs are attached to the main document.
 * 
 * @param dom server response as a XML DOM document 
 * @param contentType enumerated content type. One of 'instances', 'domain', 'properties' or 'error'
 * 
 * @returns an array of unattached divs only one of which is visible.
 */
function renderXMLResponse(/*XML DOM*/dom, /*enum*/contentType) {
	var displayModes = getSupportedDisplayModes('xml', contentType);
	var newDivs = new Array(displayModes.length);
	for (var i = 0; i < displayModes.length; i++) {
		var displayMode = displayModes[i];
		if (displayMode == 'xml') {
			newDivs[i] = renderXMLasXML(dom);
		} else if (contentType == 'instances') {
			if (displayMode == 'html') {
				newDivs[i] = renderInstancesFromXMLAsHTML(dom);
			} else if (displayMode == 'dojo') {
				newDivs[i] = renderInstancesFromXMLAsDojo(dom);
			}
		} else if (contentType == 'domain') {
			if (displayMode == 'html') {
				newDivs[i] = renderDomainFromXMLAsHTML(dom);
			} else if (displayMode == 'dojo') {
				newDivs[i] = renderDomainFromXMLAsDojo(dom);
			}
		} else if (contentType == 'properties') {
			newDivs[i] = renderPropertiesFromXMLAsHTML(dom);
		} 
		newDivs[i].style.display = (i == 0 ? 'block' : 'none');
		newDivs[i].setAttribute("id", "display.mode." + displayMode);
	}
	return newDivs;
}

/**
 * Renders the given instance data in the format of a XML DOM document into a set of Dojo widgets
 * inside a div element.  
 * 
 * @param data the root node of a XML document containing instances data
 * 
 * @returns an unattached div containing a set of dojo widgets
 */
function renderInstancesFromXMLAsDojo(/* XML DOM*/data) {
	var target = document.createElement('div');
    var panels = new Array();
    dojo.query("instance", data).forEach(function(item, index) {
    	  var panel = createInstanceDojoWidget(item);
    	  panels[index] = panel;
    });

    // assign random location to each panel and add them to canvas
    dojo.forEach(panels, function(item, index) {
    	var domNode = item.domNode;
    	domNode.style.width = "200px";
    	domNode.style.position = "absolute";
    	domNode.style.left  = 100 + (index % 5)*300 + "px";
    	domNode.style.top   = 100 + Math.floor(index / 5)*200 +"px";
    	target.appendChild(domNode);
    });
    return target;
}

/**
 * Renders given DOM for metamodel as dojo widgets.
 * 
 * @param data XML DOM for domain model.
 * @returns a HTML div 
 */
function renderDomainFromXMLAsDojo(/*XML DOM*/data) {
	var target = document.createElement('div');
    var panels = new Array();
    dojo.query("entity, embeddable, mapped-superclass", data)
        .forEach(function(item, index) {
    	  var panel = createEntityTypeDojoWidget(item);
    	  panels[index] = panel;
    });

    // assign random location to each panel and add them to canvas
    dojo.forEach(panels, function(item, index) {
    	var domNode = item.domNode;
    	domNode.style.width = "200px";
    	domNode.style.position = "absolute";
    	domNode.style.left  = 100 + (index % 5)*300 + "px";
    	domNode.style.top   = 100 + Math.floor(index / 5)*200 +"px";
    	target.appendChild(domNode);
    });
    return target;
}

/**
 * Renders given XML DOM for instances to HTML tables.
 * *** NOT IMPLEMENTED
 * 
 * @param data XML DOM for list of instances. All instanes may not belong to same entity.
 * @returns a div with zero or more tables.
 */
function renderInstancesFromXMLAsHTML(/* XML DOM */data) {
	return unimplemented("Rendering Instances as HTML is not implemented");
}

/**
 * Renders given XML DOM for domain model to HTML tables.
 * *** NOT IMPLEMENTED
 * 
 * @param data XML DOM for list of domain model. 
 * @returns a div with zero or more tables.
 */
function renderDomainFromXMLAsHTML(/* XML DOM */data) {
	return unimplemented("Rendering Domain as HTML is not implemented");
}

function unimplemented(/*string*/message) {
	var empty = document.createElement('img');
	empty.setAttribute("src", "images/underconstruction.jpg");
	empty.setAttribute("alt", message);
	return empty;
}

/**
 * Renders configration (name-value pairs) in a HTML table.
 * 
 * @param data XML DOM for name-value pair properties.
 * @returns a HTML table
 */
function renderPropertiesFromXMLAsHTML(/* XML DOM */data) {
	var table = document.createElement("table");
	var caption = document.createElement("caption");
	caption.innerHTML = "Configuration Properties";
	table.appendChild(caption);
	dojo.query("property", data)
	    .forEach(function(item, index) {
		  var row = document.createElement("tr");
		  row.className = index%2 == 0 ? 'even' : 'odd'; 
		  var key = document.createElement("td");
		  var val = document.createElement("td");
		  key.innerHTML = item.getAttribute("name");
		  val.innerHTML = item.getAttribute("value");
		  row.appendChild(key);
		  row.appendChild(val);
		 table.appendChild(row);
	    }
	);
	return table;
}

/**
 * Renders error message in HTML
 * 
 * @param data XML DOM containing error description
 * 
 * @returns a div element with error details
 */
function renderErrorFromXMLAsHTML(/*response as XML DOM*/responseXML, ioargs) {
	var div    = document.createElement("div");
	var ecode  = document.createElement("h3");
	var header = document.createElement("p");
	var msg    = document.createElement("p");
	var trace  = document.createElement("pre");
	ecode.setAttribute("class", "error-header");
	header.setAttribute("class", "error-message");
	msg.setAttribute("class", "error-message");
	
	var serverError = responseXML.documentElement;
	ecode.innerHTML  = "HTTP Error " + ioargs.xhr.status;
	header.innerHTML = dojox.xml.parser.textContent(serverError.getElementsByTagName("error-header").item(0));
	msg.innerHTML    = dojox.xml.parser.textContent(serverError.getElementsByTagName("error-message").item(0));
	trace.innerHTML  = dojox.xml.parser.textContent(serverError.getElementsByTagName("stacktrace").item(0));
	div.appendChild(ecode);
	div.appendChild(header);
	div.appendChild(msg);
	div.appendChild(trace);

	return div;
}

/**
 * Creates a dojo Title Pane from a DOM instance node. The pane has the instance
 * id as its title. The content is a table with name and value of each attribute 
 * in each row. Multi-cardinality values are in separate row without the attribute
 * name repeated except the first row.
 * 
 * @param instanceNode an XML instance node
 * 
 * @returns dojo widget for a single instance
 */
function createInstanceDojoWidget(/*XML node*/instanceNode) {
	var instanceTable = document.createElement("table");
	dojo.query('id, basic, enum, version', instanceNode)
	    .forEach(function(item) { 
			var attrRow     = document.createElement("tr");
			var nameColumn  = document.createElement("td");
			var valueColumn = document.createElement("td");
			nameColumn.className  = item.nodeName.toLowerCase(); /* May be cross-browser trouble */
			nameColumn.innerHTML  = item.getAttribute("name");
			valueColumn.innerHTML = dojox.xml.parser.textContent(item);
			attrRow.appendChild(nameColumn);
			attrRow.appendChild(valueColumn);
			instanceTable.appendChild(attrRow);
		}
	);
	dojo.query('one-to-one, many-to-one', instanceNode)
	    .forEach(function(item) { 
		var attrRow     = document.createElement("tr");
		var nameColumn  = document.createElement("td");
		var valueColumn = document.createElement("td");
		nameColumn.className = item.nodeName.toLowerCase(); /* May be cross-browser trouble */
		nameColumn.innerHTML = item.getAttribute("name");
		dojo.query('ref, null', instanceNode)
		    .forEach(function(ref) {
				valueColumn.innerHTML = ref.nodeName == 'null' ? 'null' : ref.getAttribute("id");
				valueColumn.className = ref.nodeName.toLowerCase();
				attrRow.appendChild(nameColumn);
				attrRow.appendChild(valueColumn);
				instanceTable.appendChild(attrRow);
		    });
    });
	dojo.query('one-to-many, many-to-many', instanceNode).forEach(function(item) { 
		var attrRow     = document.createElement("tr");
		var nameColumn  = document.createElement("td");
		var valueColumn = document.createElement("td");
		nameColumn.className = item.nodeName.toLowerCase(); /* May be cross-browser trouble */
		nameColumn.innerHTML = item.getAttribute("name");
		var refs = item.getElementsByTagName("ref");
		for (var i = 0; i < refs.length; i++) {
			if (i == 0) {
				valueColumn.innerHTML = refs[i].getAttribute("id");
				valueColumn.className = refs[i].nodeName.toLowerCase();
				attrRow.appendChild(nameColumn);
				attrRow.appendChild(valueColumn);
				instanceTable.appendChild(attrRow);
			} else {
				var attrRow     = document.createElement("tr");
				var nameColumn  = document.createElement("td"); // empty column
				var valueColumn = document.createElement("td");
				valueColumn.className = refs[i].nodeName.toLowerCase();
				valueColumn.innerHTML = refs[i].getAttribute("id");
				attrRow.appendChild(nameColumn);
				attrRow.appendChild(valueColumn);
				instanceTable.appendChild(attrRow);
			}
		}
      }
    );
	
	var pane = new dijit.TitlePane({title:instanceNode.getAttribute("id"),content:instanceTable});
	return pane;
}


/**
 * Creates a dojo Title Pane from a DOM instance node. The pane has the instance
 * id as its title. The content is name and value of each attribute in separate
 * line.
 * 
 * @param node
 *            an instance node
 * @returns
 */
function createEntityTypeDojoWidget(node) {
	var entityTable = document.createElement("table");
	dojo.query('id, basic, enum, version, one-to-one, many-to-one, one-to-many, many-to-many', node)
        .forEach(function(item) { 
			var attr = document.createElement("tr");
			var name = document.createElement("td");
			name.className = item.nodeName.toLowerCase(); /* May be cross-browser trouble */
			var type = item.getAttribute("type");
			name.innerHTML = type;
			if (name.className == 'one-to-many') {
			     name.innerHTML = type + '&lt;' + item.getAttribute("member-type") + '&gt;';
			}
			var value = document.createElement("td");
			value.innerHTML = dojox.xml.parser.textContent(item);
			attr.appendChild(name);
			attr.appendChild(value);
			entityTable.appendChild(attr);
		}
	);
    
    var pane = new dijit.TitlePane({title:node.getAttribute("name"), content: entityTable});
	return pane;
}

/**
 * Generic routine to render the given XML Document as a raw but indented text in to an unattached div section.
 * 
 * @param dom a XML DOM
 * 
 * @returns an unattached div section
 */
function renderXMLasXML(/*XML DOM*/dom) {
	var newDiv = document.createElement('div');
	print(dom.documentElement, newDiv, 0);
	return newDiv;
}

/**
 * Renders a XML DOM node as a new child of the given HTML node.
 * 
 * CSS styles used: 
 * node-value : The value of a text node
 * attr-name  : The name of an attribute
 * attr-value : The value of an attribute
 * delimiter  : symbols such = " < \ > used in visual XML
 * the XML element name : e.g. A <metamodel> tag will be decorated with .metamodel CSS style    
 *  
 */
function print(/* XML node */xnode, /* HTML node*/ hnode, /*int*/counter) {
	if (xnode.nodeName == '#text') {
		addTextNode(hnode, xnode.nodeValue, "node-value");
		return;
	}
	var root = document.createElement('div');
	root.style.position = 'relative';
	root.style.left = '2em';
	addRoot(xnode, hnode, root, ++counter);
	
	var attrs = xnode.attributes;
	if (attrs) {
	 	for (var i = 0; i < attrs.length; i++) {
			var attr = attrs[i];
			addTextNode(root, ' ' + attr.nodeName, "attr-name");
			addDelimiter(root, '=');
			addTextNode(root, '"' + attr.nodeValue + '"', "attr-value");
		}
	 	addDelimiter(root, '>');
	}
	var children = xnode.childNodes;
	if (children) {
		for (var i = 0; i < children.length; i++) {
			print(children[i], root, ++counter);
		}
	}
	addDelimiter(root, '</');
	addTextNode(root, xnode.nodeName, xnode.nodeName);
	addDelimiter(root, '>');
    return;	
}

/**
 * Adds the given delimiter text with CSS style 'delimiter' to the given parent node
 * @param parentNode
 * @param text
 */
function addDelimiter(/* HTML node*/ parentNode, /* Delimiter String*/ delim) {
	addTextNode(parentNode, delim, 'delimiter');
}
/**
 * Adds a <span> node of given className to the given parentNode with the given text.
 * 
 * @param parentNode the parent node to which new text is added as a <span> element.
 * @param text text to be added to the new <span> element
 * @param className class of the new <span> element
 * @returns the new <span> node
 */
function addTextNode(/* HTML node*/parentNode, /* String */text, /* String*/className) {
	if (isEmpty(text)) return null;
	newNode = document.createElement('span');
	if (className) {  
		newNode.className  = className;
	}
	if (text) { 
		newNode.appendChild(document.createTextNode(text)); 
	}
	if (parentNode) { 
		parentNode.appendChild(newNode); 
	}
	return newNode;		
}
function isTextNode(/* XML node */ xnode) {
	return xnode == null || xnode.nodeName == '#text';
}

function isTogglable(/* XML node */ xnode) {
	if (xnode == null) return false;
	if (isTextNode(xnode)) return false;
	var children = xnode.childNodes;
	if (children == null || children.length == 0) return false;
	if (children.length == 1 && isTextNode(children[0])) return false;
	return true;
}

function addRoot(xnode, hnode, root, counter) {
   if (isTogglable(xnode)) {
	   hnode.appendChild(document.createElement('br'));
		var ctrl = addTextNode(hnode, '-');
		root.setAttribute("id", counter);
		var moniker = '&lt;' + xnode.nodeName + '&gt;...';
		ctrl.setAttribute("onclick", 'javascript:toggle(this, "' + moniker + '", "' + counter + '");');
   } 
   addDelimiter(root, '<');
   addTextNode(root, xnode.nodeName, xnode.nodeName);
   hnode.appendChild(root);
   
}

function toggle(/* HTML node */ctrl, /* id */ moniker, /* id */ targetId) {
	var visible = ctrl.innerHTML == '-';
	ctrl.innerHTML = visible ? '+' + moniker : '-';
	var target = document.getElementById(targetId);
	if (visible) {
		target.style.display = "none";
	} else {
		target.style.display = "inline";
	}
}


/**
 * Renders server response of JSON objects.
 * Server sends always an array of JSON objects.
 * @param json an array of hopefully non-empty array of JSON objects
 * @param contentType type of content. Currently only instances are JSONized.
 * @returns an array of div with a single member
 */
function renderJSONResponse(/*JSON[]*/json, /*enum*/contentType) {
	var text = dojo.toJson(json, true);
	var div = document.createElement("div");
	var pre = document.createElement("pre");
	pre.innerHTML = text;
	div.appendChild(pre);
	return [div]; // an array of a single div
}

/**
 * Help related utilities.
 */

var helpDialog;

function createDialog() {
	if (helpDialog == null) {
		helpDialog = new dijit.Dialog({style: "width: 400px; height:300px;overflow:auto"});
	}
	return helpDialog;
}

function showHelp(title, href) {
	var dialog = createDialog();
	dialog.set("title", title);
	dialog.set("href", href);
	dialog.show();
}


