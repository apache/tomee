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
 
/**
 * Shows a page in the right division.
 */
function showPage(id, body) {
  var el = document.getElementById(id);
  el.innerHTML = body;
}

/**
 * Pops up a new window.
 */
function popup(url) {
	newwindow = window.open(url,'SourceCode','height=300,width=1500,left=200,top=800,scrollbars=yes');
	if (window.focus) {
	   newwindow.focus()
	}
	return false;
}

/**
  * All hyperlink reference tags of type = 'popup' is re-written
  * with onclick event handler to pop up a new window with 
  * popup() function defined in this script  
  */
function onload() {
   var x = document.getElementsByTagName('a');
   for (var i=0; i<x.length; i++) {
	  if (x[i].getAttribute('type') == 'popup') {
		x[i].onclick = function () {
			return popup(this.href)
		}
	  }
   }
   var y = document.getElementsByTagName('A');
   for (var i=0; i<y.length; i++) {
	  if (y[i].getAttribute('type') == 'popup') {
		y[i].onclick = function () {
			return popup(this.href)
		}
	  }
   }
}




