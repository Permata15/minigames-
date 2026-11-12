/* Level A2 – Hafenstadt (Kota Pelabuhan) */
window.GRAMMATIK_LEVELS = window.GRAMMATIK_LEVELS || [];
window.GRAMMATIK_LEVELS.push({
  id: 'A2',
  title: 'Hafenstadt',
  subtitle: 'Kota Pelabuhan',
  icon: '⚓',
  color: '#2d8cdc',
  intro: 'Kamu tiba di <b>Hafenstadt</b>, kota pelabuhan yang ramai. Para pelaut bercerita tentang masa lalu, tapi <b>Kasus-Krake</b> mengacak semua Dativ di lautan! Kuasai Perfekt, Dativ, dan anak kalimat untuk berlayar lebih jauh.',
  boss: ['Die Kasus-Krake', '🐙'],
  topics: [
    {
      id: 'a2-1',
      title: 'Perfekt',
      sub: 'Bentuk lampau dalam percakapan: haben/sein + Partizip II',
      monster: ['Vergangenheits-Geist', '👻'],
      theory: `
        <p><b>Perfekt</b> adalah bentuk lampau yang paling sering dipakai saat <b>berbicara</b>. Rumusnya:</p>
        <p style="text-align:center"><b>haben / sein</b> (posisi 2) … <b>Partizip II</b> (akhir)</p>
        <h3>Membentuk Partizip II</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Jenis</th><th>Pola</th><th>Contoh</th></tr>
          <tr><td>beraturan</td><td>ge- … -t</td><td>machen → <span class="de">gemacht</span>, kaufen → gekauft, arbeiten → gearbeitet</td></tr>
          <tr><td>tidak beraturan</td><td>ge- … -en (vokal bisa berubah)</td><td>essen → <span class="de">gegessen</span>, trinken → getrunken, schreiben → geschrieben</td></tr>
          <tr><td>bisa dipisah</td><td>awalan + ge + …</td><td>einkaufen → ein<b>ge</b>kauft, aufstehen → auf<b>ge</b>standen</td></tr>
          <tr><td>be-, ver-, er-, ent- … & -ieren</td><td><b>tanpa</b> ge-</td><td>besuchen → besucht, verstehen → verstanden, studieren → studiert</td></tr>
        </table></div>
        <h3>haben atau sein?</h3>
        <ul>
          <li><b>sein</b>: kata kerja <b>gerak dari A ke B</b> (gehen, fahren, kommen, fliegen, laufen) dan <b>perubahan keadaan</b> (aufstehen, einschlafen, sterben, werden), serta <i>sein, bleiben, passieren</i>.</li>
          <li><b>haben</b>: hampir semua kata kerja lainnya (termasuk yang memiliki objek Akkusativ).</li>
        </ul>`,
      examples: [
        ['Ich habe gestern Pizza gegessen.', 'Kemarin saya makan pizza.'],
        ['Wir sind nach Bali geflogen.', 'Kami terbang ke Bali.'],
        ['Hast du die Hausaufgaben gemacht?', 'Apakah kamu sudah mengerjakan PR?'],
        ['Er ist um sechs Uhr aufgestanden.', 'Dia bangun jam enam.'],
        ['Sie hat Medizin studiert.', 'Dia (pr) kuliah kedokteran.']
      ],
      ex: [
        ['m', 'Gestern ___ ich ins Kino gegangen.', ['bin', 'habe', 'hat', 'ist'], 'gehen = gerak → sein; ich → bin'],
        ['m', 'Wir haben einen Brief ___.', ['geschrieben', 'geschreibt', 'schrieben', 'geschriebt'], 'schreiben (tidak beraturan) → geschrieben'],
        ['m', 'Hast du meine Nachricht ___?', ['bekommen', 'gebekommen', 'bekommt', 'gekommen'], 'Awalan be- → tanpa ge-: bekommen'],
        ['f', 'Ich habe gestern Brot ___. (kaufen)', ['gekauft'], 'beraturan: ge-kauf-t'],
        ['f', 'Der Zug ist pünktlich ___. (ankommen)', ['angekommen'], 'bisa dipisah: an-ge-kommen'],
        ['m', 'Was ___ passiert?', ['ist', 'hat', 'sind', 'haben'], 'passieren → Perfekt dengan sein'],
        ['o', 'Wir haben im Urlaub viel fotografiert.', 'Kami banyak memotret saat liburan.', 'Kata kerja -ieren → Partizip II tanpa ge-']
      ]
    },
    {
      id: 'a2-2',
      title: 'Dativ',
      sub: 'Objek tidak langsung: Wem? + preposisi Dativ',
      monster: ['Dativ-Hai', '🦈'],
      theory: `
        <p><b>Dativ</b> menjawab pertanyaan <i>Wem?</i> (kepada siapa). Biasanya untuk <b>penerima</b>: <i>Ich gebe <b>dem Kind</b> (Dativ) <b>ein Buch</b> (Akkusativ).</i></p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>maskulin</th><th>feminin</th><th>neutral</th><th>Plural</th></tr>
          <tr><td>tertentu</td><td><span class="de">dem</span></td><td><span class="de">der</span></td><td><span class="de">dem</span></td><td><span class="de">den</span> + -n</td></tr>
          <tr><td>tak tentu / kein</td><td>einem</td><td>einer</td><td>einem</td><td>keinen + -n</td></tr>
        </table></div>
        <p><b>Pronomen Dativ</b>: mir, dir, ihm, ihr, ihm, uns, euch, ihnen / Ihnen</p>
        <h3>Kata kerja + Dativ</h3>
        <p>helfen, danken, gefallen, gehören, schmecken, antworten, gratulieren, passen, zuhören</p>
        <h3>Preposisi yang selalu + Dativ</h3>
        <p><b>aus, bei, mit, nach, seit, von, zu</b>, gegenüber</p>
        <div class="tip">💡 Gabungan: zu dem = <b>zum</b>, zu der = <b>zur</b>, von dem = <b>vom</b>, bei dem = <b>beim</b>.<br>Plural Dativ menambah <b>-n</b>: mit den Kinder<b>n</b>.</div>`,
      examples: [
        ['Ich gebe dem Kind ein Buch.', 'Saya memberi anak itu sebuah buku.'],
        ['Das Buch gehört mir.', 'Buku itu milik saya.'],
        ['Ich fahre mit dem Bus.', 'Saya naik bus.'],
        ['Wir helfen unserer Mutter.', 'Kami membantu ibu kami.'],
        ['Ich gehe zum Arzt.', 'Saya pergi ke dokter.']
      ],
      ex: [
        ['m', 'Ich helfe ___ Mann.', ['dem', 'den', 'der', 'des'], 'helfen + Dativ; der Mann → dem'],
        ['m', 'Wie gefällt ___ die Stadt? (du)', ['dir', 'dich', 'du', 'dein'], 'gefallen + Dativ → dir'],
        ['m', 'Sie wohnt bei ___ Tante.', ['ihrer', 'ihre', 'ihren', 'ihrem'], 'bei + Dativ; die Tante → ihrer'],
        ['f', 'Ich fahre mit ___ Fahrrad zur Arbeit. (das)', ['dem'], 'mit + Dativ; das Fahrrad → dem'],
        ['f', 'Die Schuhe gehören ___. (ich)', ['mir'], 'gehören + Dativ → mir'],
        ['m', 'Er schenkt ___ Kindern Bonbons.', ['den', 'die', 'der', 'dem'], 'Dativ Plural → den + -n (Kindern)'],
        ['o', 'Ich gehe nach der Arbeit zum Arzt.', 'Saya pergi ke dokter setelah bekerja.', 'nach + Dativ (der Arbeit); zum = zu dem']
      ]
    },
    {
      id: 'a2-3',
      title: 'Wechselpräpositionen',
      sub: 'Preposisi dua arah: Wo? (Dativ) – Wohin? (Akkusativ)',
      monster: ['Möwe der Richtung', '🦅'],
      theory: `
        <p>Sembilan preposisi ini bisa memakai <b>Akkusativ atau Dativ</b>: <b>in, an, auf, über, unter, vor, hinter, neben, zwischen</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Pertanyaan</th><th>Arti</th><th>Kasus</th><th>Contoh</th></tr>
          <tr><td><b>Wohin?</b></td><td>ke mana (gerakan/arah)</td><td>Akkusativ</td><td>Ich lege das Buch auf <span class="de">den</span> Tisch.</td></tr>
          <tr><td><b>Wo?</b></td><td>di mana (posisi)</td><td>Dativ</td><td>Das Buch liegt auf <span class="de">dem</span> Tisch.</td></tr>
        </table></div>
        <h3>Pasangan kata kerja penting</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Wohin? (aksi) + Akk</th><th>Wo? (keadaan) + Dat</th></tr>
          <tr><td>legen (meletakkan berbaring)</td><td>liegen (terletak)</td></tr>
          <tr><td>stellen (meletakkan berdiri)</td><td>stehen (berdiri)</td></tr>
          <tr><td>setzen (mendudukkan)</td><td>sitzen (duduk)</td></tr>
          <tr><td>hängen (menggantung, aksi)</td><td>hängen (tergantung)</td></tr>
        </table></div>
        <div class="tip">💡 in das = <b>ins</b>, in dem = <b>im</b>, an das = <b>ans</b>, an dem = <b>am</b>.</div>`,
      examples: [
        ['Ich lege das Buch auf den Tisch.', 'Saya meletakkan buku di atas meja.'],
        ['Das Buch liegt auf dem Tisch.', 'Buku itu ada di atas meja.'],
        ['Wir gehen ins Kino.', 'Kami pergi ke bioskop.'],
        ['Wir sind im Kino.', 'Kami (sedang) di bioskop.'],
        ['Das Bild hängt an der Wand.', 'Lukisan itu tergantung di dinding.']
      ],
      ex: [
        ['m', 'Ich stelle die Vase auf ___ Tisch.', ['den', 'dem', 'der', 'das'], 'stellen = aksi (Wohin?) → Akkusativ: den Tisch'],
        ['m', 'Die Katze schläft unter ___ Bett.', ['dem', 'das', 'den', 'der'], 'schlafen = posisi (Wo?) → Dativ; das Bett → dem'],
        ['m', 'Am Samstag gehen wir ___ Kino.', ['ins', 'im', 'in', 'am'], 'gehen = arah → in das = ins'],
        ['f', 'Der Schlüssel steckt in ___ Tür. (die)', ['der'], 'Wo? → Dativ; die Tür → der'],
        ['m', 'Ich ___ das Buch ins Regal.', ['stelle', 'stehe', 'liege', 'sitze'], 'Ada Akkusativ (ins Regal) → kata kerja aksi: stellen'],
        ['f', 'Er hängt die Jacke an ___ Haken. (der Haken, Wohin?)', ['den'], 'Wohin? → Akkusativ maskulin → den'],
        ['o', 'Wir sitzen im Garten unter dem Baum.', 'Kami duduk di kebun di bawah pohon.', 'Wo? → Dativ (im Garten, unter dem Baum)']
      ]
    },
    {
      id: 'a2-4',
      title: 'Possessivartikel',
      sub: 'Kata ganti milik: mein, dein, sein, ihr …',
      monster: ['Besitz-Elster', '🦜'],
      theory: `
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Pemilik</th><th>ich</th><th>du</th><th>er / es</th><th>sie</th><th>wir</th><th>ihr</th><th>sie</th><th>Sie</th></tr>
          <tr><td>Possessiv</td><td>mein</td><td>dein</td><td>sein</td><td>ihr</td><td>unser</td><td>euer</td><td>ihr</td><td>Ihr</td></tr>
        </table></div>
        <p>Akhirannya mengikuti <b>ein/kein</b> dan bergantung pada <b>benda yang dimiliki</b> (bukan pemiliknya):</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>maskulin</th><th>feminin</th><th>neutral</th><th>Plural</th></tr>
          <tr><td>Nominativ</td><td>mein</td><td>meine</td><td>mein</td><td>meine</td></tr>
          <tr><td>Akkusativ</td><td>meinen</td><td>meine</td><td>mein</td><td>meine</td></tr>
          <tr><td>Dativ</td><td>meinem</td><td>meiner</td><td>meinem</td><td>meinen (+n)</td></tr>
        </table></div>
        <div class="tip">💡 <i>sein</i> = milik dia laki-laki/netral, <i>ihr</i> = milik dia perempuan atau mereka.<br><i>euer</i> kehilangan -e- bila ada akhiran: <b>eure</b> Kinder, <b>euren</b> Hund.</div>`,
      examples: [
        ['Das ist mein Bruder.', 'Ini saudara laki-laki saya.'],
        ['Wo ist deine Tasche?', 'Di mana tasmu?'],
        ['Er besucht seine Eltern.', 'Dia (lk) mengunjungi orang tuanya.'],
        ['Sie ruft ihren Vater an.', 'Dia (pr) menelepon ayahnya.'],
        ['Wir fahren mit unserem Auto.', 'Kami pergi dengan mobil kami.']
      ],
      ex: [
        ['m', 'Lisa sucht ___ Schlüssel. (milik Lisa)', ['ihren', 'seinen', 'ihre', 'ihr'], 'Pemilik perempuan → ihr-; der Schlüssel Akkusativ → ihren'],
        ['m', 'Tom liebt ___ Katze.', ['seine', 'ihre', 'sein', 'seinen'], 'Pemilik laki-laki → sein-; die Katze → seine'],
        ['m', 'Kinder, wo sind ___ Jacken?', ['eure', 'euere', 'euer', 'euren'], 'ihr → euer → eure (Plural, -e- hilang)'],
        ['f', 'Ich spreche mit ___ Mutter. (mein-)', ['meiner'], 'mit + Dativ; feminin → meiner'],
        ['f', 'Herr Klein, ist das ___ Koffer? (Ihr-)', ['Ihr'], 'Formal: Ihr (huruf besar); der Koffer Nominativ → Ihr'],
        ['m', 'Wir besuchen morgen ___ Großeltern.', ['unsere', 'unser', 'unseren', 'unserem'], 'Plural Akkusativ → unsere'],
        ['o', 'Unsere Lehrerin erklärt die Grammatik.', 'Guru (pr) kami menjelaskan tata bahasa.', 'unser + e (die Lehrerin, feminin)']
      ]
    },
    {
      id: 'a2-5',
      title: 'Komparativ & Superlativ',
      sub: 'Membandingkan: größer als, am größten',
      monster: ['Vergleichs-Krokodil', '🐊'],
      theory: `
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Positiv</th><th>Komparativ (+ als)</th><th>Superlativ</th></tr>
          <tr><td>schnell</td><td>schnell<b>er</b></td><td>am schnell<b>sten</b></td></tr>
          <tr><td>alt</td><td><b>ä</b>lter</td><td>am <b>ä</b>ltesten</td></tr>
          <tr><td>groß</td><td>gr<b>ö</b>ßer</td><td>am gr<b>ö</b>ßten</td></tr>
          <tr><td>jung</td><td>j<b>ü</b>nger</td><td>am j<b>ü</b>ngsten</td></tr>
          <tr><td><b>gut</b></td><td><span class="de">besser</span></td><td><span class="de">am besten</span></td></tr>
          <tr><td><b>viel</b></td><td><span class="de">mehr</span></td><td><span class="de">am meisten</span></td></tr>
          <tr><td><b>gern</b></td><td><span class="de">lieber</span></td><td><span class="de">am liebsten</span></td></tr>
          <tr><td><b>hoch</b></td><td><span class="de">höher</span></td><td><span class="de">am höchsten</span></td></tr>
        </table></div>
        <ul>
          <li>Lebih … dari → Komparativ + <b>als</b>: <i>Berlin ist größer als München.</i></li>
          <li>Sama … dengan → <b>so / genauso … wie</b>: <i>Ich bin so groß wie mein Vater.</i></li>
          <li>Superlativ di depan kata benda: <i>der höchste Berg, das beste Buch</i>.</li>
        </ul>
        <div class="tip">💡 Kata sifat berakhiran -t, -d, -s, -ß, -z mendapat -e- di Superlativ: am ält<b>e</b>sten, am heiß<b>e</b>sten.</div>`,
      examples: [
        ['Berlin ist größer als München.', 'Berlin lebih besar daripada München.'],
        ['Ich bin so groß wie mein Vater.', 'Saya sama tingginya dengan ayah saya.'],
        ['Ich trinke lieber Tee als Kaffee.', 'Saya lebih suka minum teh daripada kopi.'],
        ['Der ICE ist am schnellsten.', 'Kereta ICE paling cepat.'],
        ['Das ist der beste Film des Jahres.', 'Ini film terbaik tahun ini.']
      ],
      ex: [
        ['m', 'Mein Bruder ist ___ als ich.', ['älter', 'alter', 'mehr alt', 'am ältesten'], 'alt → älter (+ Umlaut) + als'],
        ['m', 'Pizza schmeckt mir gut, aber Sushi schmeckt mir ___.', ['besser', 'guter', 'am gutesten', 'mehr gut'], 'gut → besser (tidak beraturan)'],
        ['m', 'Anna ist genauso alt ___ Maria.', ['wie', 'als', 'wenn', 'dass'], 'Kesamaan → so / genauso … wie'],
        ['f', 'Im Sommer ist es ___ als im Winter. (warm)', ['wärmer'], 'warm → wärmer'],
        ['f', 'Ich spiele am ___ Fußball. (gern)', ['liebsten'], 'gern → lieber → am liebsten'],
        ['m', 'Wer hat ___ Geld: Tom oder Paul?', ['mehr', 'mehrer', 'vieler', 'meisten'], 'viel → mehr (Komparativ, tidak beraturan)'],
        ['o', 'Die Zugspitze ist der höchste Berg Deutschlands.', 'Zugspitze adalah gunung tertinggi di Jerman.', 'hoch → höher → der höchste']
      ]
    },
    {
      id: 'a2-6',
      title: 'Nebensätze: weil, dass, wenn, ob',
      sub: 'Anak kalimat – kata kerja pindah ke akhir',
      monster: ['Nebensatz-Natter', '🐍'],
      theory: `
        <p>Setelah konjungsi berikut, <b>kata kerja terkonjugasi pindah ke akhir</b> anak kalimat:</p>
        <ul>
          <li><b>weil</b> = karena · <b>dass</b> = bahwa · <b>wenn</b> = jika / setiap kali · <b>ob</b> = apakah (pertanyaan tak langsung)</li>
        </ul>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Induk kalimat</th><th>Anak kalimat</th></tr>
          <tr><td>Ich lerne Deutsch,</td><td>weil ich in Deutschland studieren <span class="de">möchte</span>.</td></tr>
          <tr><td>Ich weiß,</td><td>dass er heute nicht <span class="de">kommt</span>.</td></tr>
          <tr><td>Er fragt,</td><td>ob du morgen Zeit <span class="de">hast</span>.</td></tr>
        </table></div>
        <p>Jika anak kalimat di <b>depan</b>, induk kalimat dimulai dengan <b>kata kerja</b>:<br><i>Wenn ich Zeit habe, <span class="de">komme</span> ich.</i></p>
        <div class="warn">⚠️ <b>denn</b> (karena) BUKAN konjungsi anak kalimat → susunan normal: <i>…, denn ich bin krank.</i></div>
        <div class="tip">💡 Kata kerja pisah menyatu kembali: <i>…, weil ich früh <b>aufstehe</b>.</i> Perfekt: <i>…, weil ich schon gegessen <b>habe</b>.</i></div>`,
      examples: [
        ['Ich bleibe zu Hause, weil ich krank bin.', 'Saya di rumah karena sakit.'],
        ['Ich glaube, dass das richtig ist.', 'Saya rasa itu benar.'],
        ['Wenn es regnet, nehme ich den Bus.', 'Kalau hujan, saya naik bus.'],
        ['Weißt du, ob der Laden offen ist?', 'Kamu tahu apakah tokonya buka?'],
        ['Ich bin müde, denn ich habe wenig geschlafen.', 'Saya lelah, karena saya kurang tidur.']
      ],
      ex: [
        ['m', 'Ich weiß, dass er heute ___.', ['nicht kommt', 'kommt nicht', 'nicht kommen', 'kommen nicht'], 'dass → kata kerja terkonjugasi di akhir'],
        ['m', '___ das Wetter schön ist, gehen wir schwimmen.', ['Wenn', 'Denn', 'Dass', 'Ob'], 'Syarat/kondisi → wenn'],
        ['m', 'Kannst du mir sagen, ___ der Zug pünktlich ist?', ['ob', 'dass', 'wenn', 'weil'], 'Pertanyaan ya/tidak tidak langsung → ob'],
        ['m', 'Ich bleibe zu Hause, denn ich ___.', ['bin krank', 'krank bin', 'krank sein', 'sein krank'], 'denn → susunan kata normal (kata kerja posisi 2)'],
        ['f', 'Wenn ich Zeit habe, ___ ich dich. (besuchen)', ['besuche'], 'Anak kalimat di depan → induk kalimat dimulai kata kerja'],
        ['o', 'Ich lerne Deutsch, weil ich in Wien arbeiten möchte.', 'Saya belajar bahasa Jerman karena ingin bekerja di Wina.', 'weil → kata kerja terkonjugasi (möchte) di akhir'],
        ['o', 'Er sagt, dass er morgen früh aufsteht.', 'Dia bilang besok dia bangun pagi.', 'Kata kerja pisah menyatu di akhir: aufsteht']
      ]
    },
    {
      id: 'a2-7',
      title: 'Reflexive Verben',
      sub: 'sich freuen, sich waschen, sich treffen …',
      monster: ['Spiegel-Kalmar', '🦑'],
      theory: `
        <p>Kata kerja refleksif memakai <b>Reflexivpronomen</b> yang merujuk kembali ke subjek.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>ich</th><th>du</th><th>er/sie/es</th><th>wir</th><th>ihr</th><th>sie/Sie</th></tr>
          <tr><td>Akkusativ</td><td>mich</td><td>dich</td><td><b>sich</b></td><td>uns</td><td>euch</td><td><b>sich</b></td></tr>
          <tr><td>Dativ</td><td><b>mir</b></td><td><b>dir</b></td><td>sich</td><td>uns</td><td>euch</td><td>sich</td></tr>
        </table></div>
        <p>Pakai <b>Dativ</b> jika dalam kalimat sudah ada objek Akkusativ lain: <i>Ich wasche <b>mich</b>.</i> ↔ <i>Ich wasche <b>mir</b> die Hände.</i></p>
        <h3>Kata kerja refleksif penting</h3>
        <p>sich freuen auf/über, sich interessieren für, sich treffen, sich beeilen, sich anziehen, sich erholen, sich setzen, sich ärgern, sich vorstellen, sich fühlen</p>`,
      examples: [
        ['Ich freue mich auf das Wochenende.', 'Saya menantikan akhir pekan.'],
        ['Er interessiert sich für Musik.', 'Dia tertarik pada musik.'],
        ['Wir treffen uns um acht Uhr.', 'Kita bertemu jam delapan.'],
        ['Beeil dich!', 'Cepatlah!'],
        ['Ich putze mir die Zähne.', 'Saya menyikat gigi.']
      ],
      ex: [
        ['m', 'Ich interessiere ___ für Geschichte.', ['mich', 'mir', 'sich', 'dich'], 'ich → mich (Akkusativ)'],
        ['m', 'Wir treffen ___ vor dem Kino.', ['uns', 'euch', 'sich', 'wir'], 'wir → uns'],
        ['m', 'Er wäscht ___ die Hände.', ['sich', 'ihm', 'ihn', 'sein'], 'er → sich (bentuk Akk & Dativ sama)'],
        ['f', 'Du putzt ___ die Zähne. (Reflexivpronomen)', ['dir'], 'Sudah ada objek Akkusativ (die Zähne) → Dativ: dir'],
        ['f', 'Ihr freut ___ auf die Ferien. (Reflexivpronomen)', ['euch'], 'ihr → euch'],
        ['m', 'Wie fühlen Sie ___ heute, Frau Lang?', ['sich', 'Sie', 'Ihnen', 'ihr'], 'Sie (formal) → sich (huruf kecil)'],
        ['o', 'Sie zieht sich schnell an.', 'Dia (pr) berpakaian dengan cepat.', 'sich anziehen: refleksif + bisa dipisah']
      ]
    }
  ]
});
