/* Level A1 – Anfangsdorf (Desa Pemula) */
window.GRAMMATIK_LEVELS = window.GRAMMATIK_LEVELS || [];
window.GRAMMATIK_LEVELS.push({
  id: 'A1',
  title: 'Anfangsdorf',
  subtitle: 'Desa Pemula',
  icon: '🏡',
  color: '#3fae5a',
  intro: 'Petualanganmu dimulai di <b>Anfangsdorf</b>, desa kecil yang tenang. Tapi sang <b>Artikel-Drache</b> telah mencuri semua kata dari penduduk desa! Pelajari dasar-dasar bahasa Jerman untuk mengembalikannya.',
  boss: ['Der Artikel-Drache', '🐉'],
  topics: [
    {
      id: 'a1-1',
      title: 'Personalpronomen & „sein“',
      sub: 'Kata ganti orang dan kata kerja "sein" (adalah/berada)',
      monster: ['Pronomen-Schnecke', '🐌'],
      theory: `
        <p><b>Personalpronomen</b> (kata ganti orang) dipakai sebagai subjek kalimat. Kata kerja <span class="de">sein</span> berarti "adalah / berada" dan bentuknya <b>tidak beraturan</b>, jadi harus dihafal.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Pronomen</th><th>Arti</th><th>sein</th></tr>
          <tr><td>ich</td><td>saya</td><td><span class="de">bin</span></td></tr>
          <tr><td>du</td><td>kamu (akrab)</td><td><span class="de">bist</span></td></tr>
          <tr><td>er / sie / es</td><td>dia (lk) / dia (pr) / dia (netral)</td><td><span class="de">ist</span></td></tr>
          <tr><td>wir</td><td>kami / kita</td><td><span class="de">sind</span></td></tr>
          <tr><td>ihr</td><td>kalian (akrab)</td><td><span class="de">seid</span></td></tr>
          <tr><td>sie / Sie</td><td>mereka / Anda (formal)</td><td><span class="de">sind</span></td></tr>
        </table></div>
        <div class="tip">💡 <b>Sie</b> (huruf besar) = "Anda" untuk orang yang belum akrab, guru, atasan. Kata kerjanya sama dengan "mereka": <i>Sie sind</i>.</div>
        <div class="warn">⚠️ Untuk profesi dan asal, tidak perlu artikel: <i>Er ist Lehrer.</i> (Dia guru.)</div>`,
      examples: [
        ['Ich bin Rina.', 'Saya Rina.'],
        ['Du bist sehr nett.', 'Kamu sangat baik.'],
        ['Er ist Lehrer.', 'Dia (laki-laki) seorang guru.'],
        ['Wir sind aus Indonesien.', 'Kami dari Indonesia.'],
        ['Sind Sie Herr Müller?', 'Apakah Anda Pak Müller?']
      ],
      ex: [
        ['m', 'Ich ___ Student.', ['bin', 'bist', 'ist', 'sind'], 'ich → bin'],
        ['m', 'Ihr ___ sehr freundlich.', ['seid', 'sind', 'seit', 'ist'], 'ihr → seid. Hati-hati: "seit" artinya "sejak".'],
        ['m', 'Frau Weber, ___ Sie aus Berlin?', ['sind', 'seid', 'ist', 'bist'], 'Sie (formal, Anda) → sind'],
        ['f', 'Das Kind ___ fünf Jahre alt. (sein)', ['ist'], 'das Kind = es → ist'],
        ['f', 'Wir ___ in Jakarta. (sein)', ['sind'], 'wir → sind'],
        ['m', 'Kata ganti untuk "Anna und ich" adalah …', ['wir', 'ihr', 'sie', 'du'], 'Anna + saya = kami → wir'],
        ['o', 'Mein Name ist Budi.', 'Nama saya Budi.', 'Kata kerja selalu di posisi ke-2 dalam kalimat berita.']
      ]
    },
    {
      id: 'a1-2',
      title: 'Präsens & „haben“',
      sub: 'Konjugasi kata kerja bentuk sekarang',
      monster: ['Verb-Wurm', '🐛'],
      theory: `
        <p>Kata kerja Jerman berubah sesuai subjek. Ambil <b>Stamm</b> (akar kata) dengan membuang <i>-en</i>, lalu tambahkan akhiran:</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>Akhiran</th><th>wohnen</th><th>arbeiten</th><th>haben</th></tr>
          <tr><td>ich</td><td>-e</td><td>wohne</td><td>arbeite</td><td><span class="de">habe</span></td></tr>
          <tr><td>du</td><td>-st</td><td>wohnst</td><td>arbeit<b>e</b>st</td><td><span class="de">hast</span></td></tr>
          <tr><td>er/sie/es</td><td>-t</td><td>wohnt</td><td>arbeit<b>e</b>t</td><td><span class="de">hat</span></td></tr>
          <tr><td>wir</td><td>-en</td><td>wohnen</td><td>arbeiten</td><td>haben</td></tr>
          <tr><td>ihr</td><td>-t</td><td>wohnt</td><td>arbeit<b>e</b>t</td><td>habt</td></tr>
          <tr><td>sie/Sie</td><td>-en</td><td>wohnen</td><td>arbeiten</td><td>haben</td></tr>
        </table></div>
        <div class="tip">💡 Akar kata yang berakhiran <b>-t / -d</b> (arbeiten, finden) mendapat tambahan <b>-e-</b>: du arbeitest, er findet.</div>
        <h3>Kata kerja dengan perubahan vokal (hanya du & er/sie/es)</h3>
        <ul>
          <li><b>e → i</b>: sprechen → du spr<b>i</b>chst, er spr<b>i</b>cht · essen → du isst, er isst · geben → du gibst</li>
          <li><b>e → ie</b>: lesen → du l<b>ie</b>st, er l<b>ie</b>st · sehen → du siehst</li>
          <li><b>a → ä</b>: fahren → du f<b>ä</b>hrst, er f<b>ä</b>hrt · schlafen → er schläft</li>
        </ul>`,
      examples: [
        ['Ich wohne in Bandung.', 'Saya tinggal di Bandung.'],
        ['Du lernst Deutsch.', 'Kamu belajar bahasa Jerman.'],
        ['Sie arbeitet im Büro.', 'Dia (pr) bekerja di kantor.'],
        ['Wir haben Hunger.', 'Kami lapar.'],
        ['Er fährt nach München.', 'Dia pergi (berkendara) ke München.']
      ],
      ex: [
        ['m', 'Du ___ gut Deutsch.', ['sprichst', 'sprechst', 'spricht', 'sprechen'], 'sprechen: e → i untuk du → du sprichst'],
        ['m', 'Er ___ ein Auto.', ['hat', 'habt', 'hast', 'haben'], 'haben tidak beraturan: er → hat'],
        ['f', 'Ihr ___ in Hamburg. (wohnen)', ['wohnt'], 'ihr → Stamm + t → wohnt'],
        ['f', 'Meine Mutter ___ jeden Tag. (arbeiten)', ['arbeitet'], 'Stamm berakhiran -t → tambah -e-: sie arbeitet'],
        ['m', 'Lena ___ ein Buch.', ['liest', 'lest', 'lesst', 'lesen'], 'lesen: e → ie → sie liest'],
        ['m', 'Der Zug ___ nach Köln.', ['fährt', 'fahrt', 'fahrst', 'fähren'], 'fahren: a → ä untuk er/sie/es → fährt'],
        ['o', 'Wir lernen heute Grammatik.', 'Kami belajar tata bahasa hari ini.', 'Subjek – kata kerja (posisi 2) – keterangan – objek.']
      ]
    },
    {
      id: 'a1-3',
      title: 'Artikel, Nominativ & Plural',
      sub: 'der, die, das – ein, eine – bentuk jamak',
      monster: ['Artikel-Fledermaus', '🦇'],
      theory: `
        <p>Setiap kata benda (Nomen) di bahasa Jerman punya <b>jenis (Genus)</b> dan selalu ditulis dengan <b>huruf besar</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>maskulin</th><th>feminin</th><th>neutral</th><th>Plural</th></tr>
          <tr><td>tertentu (the)</td><td><span class="de">der</span> Tisch</td><td><span class="de">die</span> Lampe</td><td><span class="de">das</span> Buch</td><td><span class="de">die</span> Bücher</td></tr>
          <tr><td>tak tentu (a)</td><td><span class="de">ein</span> Tisch</td><td><span class="de">eine</span> Lampe</td><td><span class="de">ein</span> Buch</td><td>– Bücher</td></tr>
        </table></div>
        <h3>Trik menebak artikel</h3>
        <ul>
          <li><b>die</b>: akhiran -ung, -heit, -keit, -schaft, -ion, -tät, -ei (die Zeitung, die Freiheit, die Information)</li>
          <li><b>das</b>: akhiran -chen, -lein, -ment, -um (das Mädchen, das Dokument, das Zentrum)</li>
          <li><b>der</b>: hari, bulan, musim, arah mata angin (der Montag, der Juli, der Sommer)</li>
        </ul>
        <div class="tip">💡 Hafalkan kata benda <b>selalu bersama artikel dan bentuk jamaknya</b>: <i>das Buch, die Bücher</i>. Artikel jamak selalu <b>die</b>.</div>`,
      examples: [
        ['Der Tisch ist groß.', 'Meja itu besar.'],
        ['Die Lampe ist neu.', 'Lampu itu baru.'],
        ['Das Mädchen heißt Mia.', 'Anak perempuan itu bernama Mia.'],
        ['Das ist ein Hund.', 'Itu seekor anjing.'],
        ['Die Bücher sind alt.', 'Buku-buku itu sudah tua.']
      ],
      ex: [
        ['m', '___ Zeitung liegt auf dem Tisch.', ['Die', 'Der', 'Das', 'Den'], 'Akhiran -ung → feminin → die Zeitung'],
        ['m', '___ Mädchen spielt im Garten.', ['Das', 'Die', 'Der', 'Ein'], 'Akhiran -chen → selalu das (walau artinya perempuan!)'],
        ['m', 'Das ist ___ Apfel.', ['ein', 'eine', 'einen', 'einer'], 'der Apfel → Nominativ maskulin → ein'],
        ['f', 'Das ist ___ Katze. (ein/eine)', ['eine'], 'die Katze (feminin) → eine'],
        ['m', 'Bentuk jamak dari "das Buch" adalah …', ['die Bücher', 'die Buchs', 'das Bücher', 'die Buche'], 'Jamak selalu memakai artikel die; Buch → Bücher'],
        ['m', 'Artikel untuk "Montag" adalah …', ['der', 'die', 'das', 'den'], 'Nama hari, bulan, dan musim → der'],
        ['f', '___ Information ist wichtig. (der/die/das, huruf besar)', ['Die'], 'Akhiran -ion → feminin → Die Information']
      ]
    },
    {
      id: 'a1-4',
      title: 'Akkusativ',
      sub: 'Objek langsung: Wen? Was?',
      monster: ['Akkusativ-Wolf', '🐺'],
      theory: `
        <p><b>Akkusativ</b> dipakai untuk <b>objek langsung</b> — jawaban dari pertanyaan <i>Wen?</i> (siapa) atau <i>Was?</i> (apa). Kabar baiknya: <b>hanya maskulin yang berubah!</b></p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>maskulin</th><th>feminin</th><th>neutral</th><th>Plural</th></tr>
          <tr><td>Nominativ</td><td>der / ein / kein</td><td>die / eine</td><td>das / ein</td><td>die / keine</td></tr>
          <tr><td>Akkusativ</td><td><span class="de">den / einen / keinen</span></td><td>die / eine</td><td>das / ein</td><td>die / keine</td></tr>
        </table></div>
        <h3>Pronomen Akkusativ</h3>
        <p>ich → <b>mich</b>, du → <b>dich</b>, er → <b>ihn</b>, sie → sie, es → es, wir → <b>uns</b>, ihr → <b>euch</b>, sie/Sie → sie/Sie</p>
        <div class="tip">💡 Kata kerja + Akkusativ yang sering dipakai: <i>haben, brauchen, kaufen, sehen, essen, trinken, möchten, kennen, suchen</i>, dan <i>es gibt</i> (ada).</div>`,
      examples: [
        ['Ich kaufe den Kuchen.', 'Saya membeli kue itu.'],
        ['Hast du einen Bruder?', 'Apakah kamu punya saudara laki-laki?'],
        ['Wir sehen die Katze.', 'Kami melihat kucing itu.'],
        ['Ich liebe dich.', 'Aku mencintaimu.'],
        ['Es gibt hier einen Park.', 'Di sini ada sebuah taman.']
      ],
      ex: [
        ['m', 'Ich brauche ___ Stift.', ['einen', 'ein', 'eine', 'einem'], 'der Stift → Akkusativ maskulin → einen'],
        ['m', 'Siehst du ___ Mann dort?', ['den', 'der', 'dem', 'des'], 'der Mann → Akkusativ → den'],
        ['m', 'Wir trinken ___ Wasser.', ['das', 'den', 'dem', 'der'], 'das Wasser (neutral) → Akkusativ tetap das'],
        ['f', 'Kennst du Peter? – Ja, ich kenne ___ gut. (er)', ['ihn'], 'er → Akkusativ: ihn'],
        ['f', 'Es gibt hier ___ Supermarkt. (ein)', ['einen'], 'es gibt + Akkusativ; der Supermarkt → einen'],
        ['m', 'Ich suche ___ Schlüssel. Wo ist er?', ['meinen', 'mein', 'meine', 'meinem'], 'der Schlüssel → Akkusativ → meinen'],
        ['o', 'Ich esse einen Apfel.', 'Saya makan sebuah apel.', 'einen = Akkusativ maskulin (der Apfel)']
      ]
    },
    {
      id: 'a1-5',
      title: 'Satzbau & Fragen',
      sub: 'Urutan kata, W-Fragen, dan Ja/Nein-Fragen',
      monster: ['Frage-Spinne', '🕷️'],
      theory: `
        <h3>1. Kalimat berita: kata kerja di posisi 2</h3>
        <p>Kata kerja terkonjugasi <b>selalu di posisi ke-2</b>. Jika keterangan diletakkan di depan, subjek pindah ke belakang kata kerja (<b>inversi</b>).</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Posisi 1</th><th>Posisi 2 (Verb)</th><th>Sisanya</th></tr>
          <tr><td>Ich</td><td><span class="de">gehe</span></td><td>heute ins Kino.</td></tr>
          <tr><td>Heute</td><td><span class="de">gehe</span></td><td>ich ins Kino.</td></tr>
        </table></div>
        <h3>2. W-Fragen: W-Wort + Verb + Subjek</h3>
        <p><b>wer</b> (siapa), <b>was</b> (apa), <b>wo</b> (di mana), <b>woher</b> (dari mana), <b>wohin</b> (ke mana), <b>wann</b> (kapan), <b>wie</b> (bagaimana), <b>warum</b> (mengapa), <b>wie viel</b> (berapa)</p>
        <h3>3. Ja/Nein-Fragen: kata kerja di posisi 1</h3>
        <p><i>Kommst du morgen?</i> – Ja. / Nein.</p>
        <div class="tip">💡 Jika pertanyaannya <b>negatif</b> dan jawabanmu positif, gunakan <span class="de">Doch!</span> — <i>Hast du keinen Hunger? – Doch!</i> (Kamu tidak lapar? – Lapar kok!)</div>`,
      examples: [
        ['Heute lerne ich Deutsch.', 'Hari ini saya belajar bahasa Jerman.'],
        ['Wo wohnst du?', 'Kamu tinggal di mana?'],
        ['Woher kommen Sie?', 'Anda berasal dari mana?'],
        ['Kommst du morgen?', 'Apakah kamu datang besok?'],
        ['Hast du keine Zeit? – Doch!', 'Kamu tidak punya waktu? – Punya kok!']
      ],
      ex: [
        ['m', '___ kommst du? – Aus Indonesien.', ['Woher', 'Wohin', 'Wo', 'Wann'], 'Menanyakan asal → woher'],
        ['m', '___ ist das? – Das ist mein Bruder.', ['Wer', 'Was', 'Wie', 'Wo'], 'Menanyakan orang → wer'],
        ['m', 'Morgen ___ nach Berlin.', ['fahre ich', 'ich fahre', 'fahren ich', 'ich fahren'], 'Keterangan di posisi 1 → kata kerja tetap posisi 2, subjek di belakangnya (inversi)'],
        ['m', 'Bist du nicht müde? – ___, ich bin sehr müde!', ['Doch', 'Ja', 'Nein', 'Nicht'], 'Jawaban positif untuk pertanyaan negatif → Doch'],
        ['f', '___ heißt du? – Ich heiße Tom. (W-Wort)', ['Wie'], 'Menanyakan nama → Wie heißt du?'],
        ['o', 'Wann beginnt der Kurs?', 'Kapan kursusnya dimulai?', 'W-Wort + kata kerja + subjek'],
        ['o', 'Am Wochenende spiele ich Fußball.', 'Di akhir pekan saya bermain sepak bola.', 'Keterangan waktu di depan → kata kerja tetap posisi 2']
      ]
    },
    {
      id: 'a1-6',
      title: 'Negation: nicht & kein',
      sub: 'Membuat kalimat negatif',
      monster: ['Nein-Skorpion', '🦂'],
      theory: `
        <h3>kein = tidak ada / bukan (untuk kata benda)</h3>
        <p>Dipakai untuk kata benda <b>tanpa artikel</b> atau dengan <b>ein/eine</b>. Akhirannya seperti <i>ein</i>:</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>maskulin</th><th>feminin</th><th>neutral</th><th>Plural</th></tr>
          <tr><td>Nominativ</td><td>kein</td><td>keine</td><td>kein</td><td>keine</td></tr>
          <tr><td>Akkusativ</td><td><span class="de">keinen</span></td><td>keine</td><td>kein</td><td>keine</td></tr>
        </table></div>
        <h3>nicht = tidak</h3>
        <p>Dipakai untuk <b>kata kerja, kata sifat, keterangan</b>, nama, atau kata benda dengan <b>der/die/das</b> dan kata ganti milik.</p>
        <ul>
          <li>Menegasikan seluruh kalimat → <b>nicht di akhir</b>: <i>Ich komme heute nicht.</i></li>
          <li>Sebelum kata sifat / keterangan tempat: <i>Das ist nicht teuer. Er wohnt nicht in Berlin.</i></li>
        </ul>
        <div class="tip">💡 Rumus cepat: <i>ein → kein</i>, tanpa artikel → <i>kein</i>, selain itu → <i>nicht</i>.</div>`,
      examples: [
        ['Ich habe keine Zeit.', 'Saya tidak punya waktu.'],
        ['Das ist kein Problem.', 'Itu bukan masalah.'],
        ['Er kommt heute nicht.', 'Dia tidak datang hari ini.'],
        ['Das Essen ist nicht teuer.', 'Makanannya tidak mahal.'],
        ['Ich habe keinen Hund.', 'Saya tidak punya anjing.']
      ],
      ex: [
        ['m', 'Ich habe ___ Auto.', ['kein', 'nicht', 'keine', 'keinen'], 'das Auto (neutral, Akk) → kein'],
        ['m', 'Das Wetter ist ___ gut.', ['nicht', 'kein', 'keine', 'keinen'], 'Menegasikan kata sifat → nicht'],
        ['m', 'Wir haben ___ Kinder.', ['keine', 'kein', 'nicht', 'keinen'], 'Plural → keine'],
        ['f', 'Er trinkt ___ Kaffee. (kein-, der Kaffee)', ['keinen'], 'der Kaffee → Akkusativ maskulin → keinen'],
        ['m', 'Ich kenne den Lehrer ___.', ['nicht', 'kein', 'keinen', 'nichts'], 'Kata benda dengan "den" → nicht, di akhir kalimat'],
        ['m', 'Das ist ___ Tasche. Meine ist rot.', ['nicht meine', 'keine meine', 'kein meine', 'meine nicht'], 'Kata ganti milik → pakai nicht sebelum frasa'],
        ['o', 'Heute habe ich keine Lust.', 'Hari ini saya sedang tidak mood.', 'kein + kata benda (die Lust → keine)']
      ]
    },
    {
      id: 'a1-7',
      title: 'Modalverben',
      sub: 'können, müssen, wollen, dürfen, sollen, möchten',
      monster: ['Modal-Kobold', '👺'],
      theory: `
        <p>Modalverben mengubah makna kata kerja lain. <b>Susunannya seperti kurung (Satzklammer)</b>: Modalverb di posisi 2, kata kerja utama (Infinitiv) di <b>akhir</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>können<br><small>bisa</small></th><th>müssen<br><small>harus</small></th><th>wollen<br><small>mau</small></th><th>dürfen<br><small>boleh</small></th><th>sollen<br><small>sebaiknya/disuruh</small></th><th>möchten<br><small>ingin (sopan)</small></th></tr>
          <tr><td>ich</td><td>kann</td><td>muss</td><td>will</td><td>darf</td><td>soll</td><td>möchte</td></tr>
          <tr><td>du</td><td>kannst</td><td>musst</td><td>willst</td><td>darfst</td><td>sollst</td><td>möchtest</td></tr>
          <tr><td>er/sie/es</td><td>kann</td><td>muss</td><td>will</td><td>darf</td><td>soll</td><td>möchte</td></tr>
          <tr><td>wir</td><td>können</td><td>müssen</td><td>wollen</td><td>dürfen</td><td>sollen</td><td>möchten</td></tr>
          <tr><td>ihr</td><td>könnt</td><td>müsst</td><td>wollt</td><td>dürft</td><td>sollt</td><td>möchtet</td></tr>
          <tr><td>sie/Sie</td><td>können</td><td>müssen</td><td>wollen</td><td>dürfen</td><td>sollen</td><td>möchten</td></tr>
        </table></div>
        <div class="warn">⚠️ Untuk <b>ich</b> dan <b>er/sie/es</b> tidak ada akhiran: <i>ich kann, er kann</i> (bukan "er kannt").</div>
        <p>Contoh struktur: <i>Ich <span class="de">kann</span> gut <span class="de">schwimmen</span>.</i></p>`,
      examples: [
        ['Ich kann gut schwimmen.', 'Saya bisa berenang dengan baik.'],
        ['Du musst jetzt gehen.', 'Kamu harus pergi sekarang.'],
        ['Wir wollen Deutsch lernen.', 'Kami mau belajar bahasa Jerman.'],
        ['Hier darf man nicht rauchen.', 'Di sini tidak boleh merokok.'],
        ['Ich möchte einen Kaffee, bitte.', 'Saya ingin secangkir kopi.']
      ],
      ex: [
        ['m', 'Er ___ sehr gut Gitarre spielen.', ['kann', 'könnt', 'kannt', 'können'], 'er → kann (tanpa akhiran -t)'],
        ['m', 'Du ___ morgen früh aufstehen.', ['musst', 'muss', 'müsst', 'müssen'], 'du → musst'],
        ['f', 'Ich ___ ein Glas Wasser, bitte. (möchten)', ['möchte'], 'ich → möchte'],
        ['m', 'Hier ___ man nicht parken. (izin/larangan)', ['darf', 'will', 'möchte', 'muss'], 'Izin / larangan → dürfen: man darf nicht = tidak boleh'],
        ['f', 'Ihr ___ hier nicht laut sein. (dürfen)', ['dürft'], 'ihr → dürft'],
        ['o', 'Wir wollen heute Abend tanzen.', 'Kami mau berdansa malam ini.', 'Modalverb posisi 2, Infinitiv di akhir'],
        ['o', 'Kannst du mir helfen?', 'Bisakah kamu membantu saya?', 'Pertanyaan Ja/Nein: Modalverb di posisi 1']
      ]
    },
    {
      id: 'a1-8',
      title: 'Trennbare Verben',
      sub: 'Kata kerja yang bisa dipisah (aufstehen, anrufen …)',
      monster: ['Trenn-Krabbe', '🦀'],
      theory: `
        <p>Beberapa kata kerja punya <b>awalan yang bisa dipisah</b>. Dalam kalimat biasa, awalannya pindah ke <b>akhir kalimat</b>. Awalan ini selalu diberi tekanan saat diucapkan.</p>
        <p>Awalan yang bisa dipisah: <b>an-, auf-, aus-, ein-, mit-, zu-, ab-, vor-, zurück-, fern-, los-, weg-</b></p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Infinitiv</th><th>Kalimat</th></tr>
          <tr><td>aufstehen (bangun)</td><td>Ich <span class="de">stehe</span> um 7 Uhr <span class="de">auf</span>.</td></tr>
          <tr><td>einkaufen (berbelanja)</td><td><span class="de">Kaufst</span> du heute <span class="de">ein</span>?</td></tr>
          <tr><td>mit Modalverb</td><td>Ich muss morgen früh <span class="de">aufstehen</span>. (digabung!)</td></tr>
        </table></div>
        <div class="warn">⚠️ Awalan <b>be-, ver-, er-, ent-, emp-, ge-, zer-, miss-</b> <u>tidak</u> bisa dipisah: <i>Ich verstehe das nicht. Er bezahlt.</i></div>`,
      examples: [
        ['Ich stehe um sieben Uhr auf.', 'Saya bangun jam tujuh.'],
        ['Der Zug fährt um neun Uhr ab.', 'Kereta berangkat jam sembilan.'],
        ['Kaufst du heute ein?', 'Apakah kamu belanja hari ini?'],
        ['Ich muss morgen früh aufstehen.', 'Saya harus bangun pagi besok.'],
        ['Ich verstehe das nicht.', 'Saya tidak mengerti itu.']
      ],
      ex: [
        ['m', 'Wann ___? (anfangen)', ['fängt der Film an', 'anfängt der Film', 'der Film fängt an', 'fängt an der Film'], 'W-Frage: W-Wort + kata kerja + subjek … awalan di akhir'],
        ['f', 'Er sieht jeden Abend ___. (fernsehen)', ['fern'], 'Awalan fern- pindah ke akhir kalimat'],
        ['f', 'Ich ___ heute im Supermarkt ein. (einkaufen)', ['kaufe'], 'ich kaufe … ein'],
        ['m', 'Ich kann morgen leider nicht ___.', ['mitkommen', 'komme mit', 'mit kommen', 'kommen mit'], 'Dengan Modalverb, Infinitiv digabung: mitkommen'],
        ['m', 'Kata kerja mana yang TIDAK bisa dipisah?', ['verstehen', 'aufstehen', 'anrufen', 'einkaufen'], 'ver- adalah awalan yang tidak bisa dipisah'],
        ['o', 'Ich rufe dich morgen an.', 'Saya akan meneleponmu besok.', 'anrufen → rufe … an'],
        ['o', 'Der Bus kommt um acht Uhr an.', 'Bus tiba jam delapan.', 'ankommen → kommt … an']
      ]
    }
  ]
});
