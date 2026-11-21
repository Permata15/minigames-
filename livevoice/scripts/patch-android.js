// Menambahkan izin & pengaturan yang dibutuhkan ke AndroidManifest setelah `npx cap add android`.
const fs = require('fs');
const path = require('path');
const file = path.join(__dirname, '..', 'android', 'app', 'src', 'main', 'AndroidManifest.xml');
let xml = fs.readFileSync(file, 'utf8');

const perms = ['android.permission.RECORD_AUDIO', 'android.permission.MODIFY_AUDIO_SETTINGS', 'android.permission.INTERNET'];
for (const p of perms) {
  if (!xml.includes(p)) xml = xml.replace('</manifest>', `    <uses-permission android:name="${p}" />\n</manifest>`);
}
if (!xml.includes('android.speech.RecognitionService')) {
  xml = xml.replace('</manifest>', `    <queries>\n        <intent><action android:name="android.speech.RecognitionService" /></intent>\n        <intent><action android:name="android.intent.action.TTS_SERVICE" /></intent>\n    </queries>\n</manifest>`);
}
// layar tetap portrait & keyboard tidak menutupi input
xml = xml.replace(/<activity\b(?![^>]*screenOrientation)/, '<activity\n            android:screenOrientation="portrait"');
xml = xml.replace(/android:windowSoftInputMode="[^"]*"/, '');
xml = xml.replace(/<activity\b/, '<activity\n            android:windowSoftInputMode="adjustResize"');
fs.writeFileSync(file, xml);
console.log('AndroidManifest patched');
