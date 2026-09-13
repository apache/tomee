/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// --- base64url <-> ArrayBuffer helpers -------------------------------------

function b64urlToBuf(value) {
    const padded = value.replace(/-/g, '+').replace(/_/g, '/');
    const binary = atob(padded);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
        bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
}

function bufToB64url(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.length; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function log(message) {
    const el = document.getElementById('log');
    if (el) {
        el.textContent += message + '\n';
    }
}

// --- first factor ----------------------------------------------------------

async function passwordStep(username, password) {
    const res = await fetch('api/login/password', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({username, password})
    });
    if (!res.ok) {
        throw new Error('Password step failed (' + res.status + ')');
    }
    log('Password accepted for ' + username);
}

// --- registration (enrol a passkey) ----------------------------------------

async function registerPasskey() {
    const optionsRes = await fetch('api/register/options');
    if (!optionsRes.ok) {
        throw new Error('Could not get registration options (' + optionsRes.status + ')');
    }
    const options = await optionsRes.json();

    // decode the server-provided base64url fields into ArrayBuffers
    options.challenge = b64urlToBuf(options.challenge);
    options.user.id = b64urlToBuf(options.user.id);
    (options.excludeCredentials || []).forEach(c => c.id = b64urlToBuf(c.id));

    const credential = await navigator.credentials.create({publicKey: options});

    const payload = {
        id: credential.id,
        rawId: bufToB64url(credential.rawId),
        type: credential.type,
        clientExtensionResults: credential.getClientExtensionResults(),
        response: {
            clientDataJSON: bufToB64url(credential.response.clientDataJSON),
            attestationObject: bufToB64url(credential.response.attestationObject),
            transports: credential.response.getTransports ? credential.response.getTransports() : []
        }
    };

    const res = await fetch('api/register', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        throw new Error('Registration failed (' + res.status + ')');
    }
    log('Passkey registered.');
}

// --- login (assert with a passkey - the 2nd factor) ------------------------

async function loginWithPasskey() {
    const optionsRes = await fetch('api/login/assertion-options');
    if (!optionsRes.ok) {
        throw new Error('Could not get assertion options (' + optionsRes.status + ')');
    }
    const options = await optionsRes.json();

    options.challenge = b64urlToBuf(options.challenge);
    (options.allowCredentials || []).forEach(c => c.id = b64urlToBuf(c.id));

    const assertion = await navigator.credentials.get({publicKey: options});

    const payload = {
        id: assertion.id,
        rawId: bufToB64url(assertion.rawId),
        type: assertion.type,
        clientExtensionResults: assertion.getClientExtensionResults(),
        response: {
            clientDataJSON: bufToB64url(assertion.response.clientDataJSON),
            authenticatorData: bufToB64url(assertion.response.authenticatorData),
            signature: bufToB64url(assertion.response.signature),
            userHandle: assertion.response.userHandle ? bufToB64url(assertion.response.userHandle) : null
        }
    };

    const res = await fetch('api/login/assertion', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        throw new Error('Passkey login failed (' + res.status + ')');
    }
    const result = await res.json();
    log('Authenticated. Following redirect to the protected page...');
    // Deliberately a fresh navigation: proves the login survived to a new request.
    window.location = result.redirect;
}
