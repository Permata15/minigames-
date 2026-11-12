/* Level B1 – Schwarzwald (Hutan Hitam) */
window.GRAMMATIK_LEVELS = window.GRAMMATIK_LEVELS || [];
window.GRAMMATIK_LEVELS.push({
  id: 'B1',
  title: 'Schwarzwald',
  subtitle: 'Hutan Hitam',
  icon: '🌲',
  color: '#2e7d4f',
  intro: 'Di dalam <b>Schwarzwald</b> yang gelap, dongeng-dongeng lama hidup kembali. Seorang <b>Endungs-Troll</b> mencuri akhiran kata sifat dan menyembunyikannya di balik pepohonan. Pelajari Präteritum, Adjektivdeklination, dan Relativsätze untuk menembus hutan ini!',
  boss: ['Der Endungs-Troll', '🧌'],
  topics: [
    {
      id: 'b1-1',
      title: 'Präteritum',
      sub: 'Bentuk lampau untuk cerita & tulisan',
      monster: ['Märchen-Rabe', '🐦'],
      theory: `
        <p><b>Präteritum</b> dipakai terutama dalam <b>tulisan</b>: cerita, dongeng, berita, laporan. Untuk <i>sein, haben</i>, dan <i>Modalverben</i>, Präteritum juga umum dalam percakapan.</p>
        <h3>Kata kerja beraturan: Stamm + -te-</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>machen</th><th>warten</th><th>gehen (tidak beraturan)</th><th>sein</th><th>haben</th></tr>
          <tr><td>ich</td><td>machte</td><td>wartete</td><td>ging</td><td>war</td><td>hatte</td></tr>
          <tr><td>du</td><td>machtest</td><td>wartetest</td><td>gingst</td><td>warst</td><td>hattest</td></tr>
          <tr><td>er/sie/es</td><td>machte</td><td>wartete</td><td>ging</td><td>war</td><td>hatte</td></tr>
          <tr><td>wir</td><td>machten</td><td>warteten</td><td>gingen</td><td>waren</td><td>hatten</td></tr>
          <tr><td>ihr</td><td>machtet</td><td>wartetet</td><td>gingt</td><td>wart</td><td>hattet</td></tr>
          <tr><td>sie/Sie</td><td>machten</td><td>warteten</td><td>gingen</td><td>waren</td><td>hatten</td></tr>
        </table></div>
        <div class="tip">💡 Bentuk <b>ich</b> dan <b>er/sie/es</b> selalu sama, dan pada kata kerja tidak beraturan <b>tanpa akhiran</b> (ich ging, er kam).</div>
        <h3>Bentuk tidak beraturan yang sering muncul</h3>
        <p>kommen → kam · sehen → sah · fahren → fuhr · lesen → las · schreiben → schrieb · finden → fand · nehmen → nahm · geben → gab · sprechen → sprach · bleiben → blieb · reiten → ritt</p>
        <p><b>Campuran</b> (berubah vokal + -te): denken → dachte · bringen → brachte · wissen → wusste · kennen → kannte</p>`,
      examples: [
        ['Es war einmal ein König.', 'Pada zaman dahulu, ada seorang raja.'],
        ['Der Junge ging allein in den Wald.', 'Anak laki-laki itu pergi sendirian ke hutan.'],
        ['Wir wohnten lange in Köln.', 'Kami lama tinggal di Köln.'],
        ['Sie kam spät nach Hause.', 'Dia (pr) pulang larut.'],
        ['Ich dachte oft an dich.', 'Aku sering memikirkanmu.']
      ],
      ex: [
        ['m', 'Gestern ___ ich keine Zeit.', ['hatte', 'habte', 'hat', 'hätte'], 'haben → hatte'],
        ['m', 'Der Prinz ___ durch den Wald.', ['ritt', 'reitete', 'ritte', 'geritten'], 'reiten (tidak beraturan) → ritt'],
        ['m', 'Wir ___ lange auf den Bus.', ['warteten', 'warten', 'wartete', 'gewartet'], 'warten → Stamm berakhiran -t → wart-ete-n'],
        ['f', 'Er ___ seiner Mutter einen Brief. (schreiben)', ['schrieb'], 'schreiben → schrieb'],
        ['f', 'Ich ___ die Antwort nicht. (wissen)', ['wusste'], 'wissen → wusste (campuran)'],
        ['m', 'Als Kinder ___ wir oft am Strand.', ['waren', 'wart', 'sind', 'warten'], 'sein → wir waren'],
        ['o', 'Als Kind spielte ich oft im Garten.', 'Waktu kecil saya sering bermain di kebun.', 'spielen → spielte (beraturan)']
      ]
    },
    {
      id: 'b1-2',
      title: 'Adjektivdeklination',
      sub: 'Akhiran kata sifat sebelum kata benda',
      monster: ['Endungs-Eule', '🦉'],
      theory: `
        <p>Kata sifat <b>di depan kata benda</b> mendapat akhiran. Ada 3 pola tergantung artikelnya.</p>
        <h3>1. Setelah der / die / das (artikel tertentu)</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>mask.</th><th>fem.</th><th>neutr.</th><th>Plural</th></tr>
          <tr><td>Nom</td><td>der alt<b>e</b> Mann</td><td>die nett<b>e</b> Frau</td><td>das klein<b>e</b> Kind</td><td>die neu<b>en</b> Bücher</td></tr>
          <tr><td>Akk</td><td>den alt<b>en</b> Mann</td><td>die nett<b>e</b> Frau</td><td>das klein<b>e</b> Kind</td><td>die neu<b>en</b> Bücher</td></tr>
          <tr><td>Dat</td><td>dem alt<b>en</b> Mann</td><td>der nett<b>en</b> Frau</td><td>dem klein<b>en</b> Kind</td><td>den neu<b>en</b> Büchern</td></tr>
        </table></div>
        <p>→ Hanya <b>-e</b> atau <b>-en</b>. Semua Dativ & Plural = <b>-en</b>.</p>
        <h3>2. Setelah ein / kein / mein …</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>mask.</th><th>fem.</th><th>neutr.</th><th>Plural (keine/meine)</th></tr>
          <tr><td>Nom</td><td>ein alt<b>er</b> Mann</td><td>eine nett<b>e</b> Frau</td><td>ein klein<b>es</b> Kind</td><td>meine neu<b>en</b> Bücher</td></tr>
          <tr><td>Akk</td><td>einen alt<b>en</b> Mann</td><td>eine nett<b>e</b> Frau</td><td>ein klein<b>es</b> Kind</td><td>meine neu<b>en</b> Bücher</td></tr>
          <tr><td>Dat</td><td>einem alt<b>en</b> Mann</td><td>einer nett<b>en</b> Frau</td><td>einem klein<b>en</b> Kind</td><td>meinen neu<b>en</b> Büchern</td></tr>
        </table></div>
        <p>→ Karena <i>ein</i> tidak menunjukkan jenis, kata sifat yang "memberi tanda": <b>-er</b> (mask.), <b>-es</b> (neutr.).</p>
        <h3>3. Tanpa artikel</h3>
        <p>Kata sifat mengambil akhiran artikel tertentu: gut<b>er</b> Wein (der), frisch<b>es</b> Brot (das), kalt<b>e</b> Milch (die), mit heiß<b>em</b> Wasser (dem).</p>
        <div class="tip">💡 Kata sifat <b>setelah kata kerja</b> tidak diberi akhiran: <i>Der Mann ist alt.</i></div>`,
      examples: [
        ['Der alte Mann liest die Zeitung.', 'Pria tua itu membaca koran.'],
        ['Ich habe einen neuen Laptop.', 'Saya punya laptop baru.'],
        ['Das ist ein schönes Haus.', 'Itu rumah yang indah.'],
        ['Mit freundlichen Grüßen', 'Salam hormat (penutup surat).'],
        ['Ich trinke gern kalte Milch.', 'Saya suka minum susu dingin.']
      ],
      ex: [
        ['m', 'Das ist ein ___ Auto.', ['schnelles', 'schnelle', 'schneller', 'schnellen'], 'ein + neutral Nominativ → -es'],
        ['m', 'Ich kaufe den ___ Mantel.', ['schwarzen', 'schwarze', 'schwarzer', 'schwarzes'], 'den (Akk maskulin) → -en'],
        ['m', 'Wir wohnen in einer ___ Wohnung.', ['kleinen', 'kleine', 'kleiner', 'kleines'], 'Dativ setelah artikel → selalu -en'],
        ['f', 'Die ___ Frau ist meine Nachbarin. (nett)', ['nette'], 'die + Nominativ tunggal → -e'],
        ['f', 'Er trinkt gern ___ Kaffee. (stark, tanpa artikel, Akk)', ['starken'], 'Tanpa artikel, Akk maskulin → seperti "den" → -en'],
        ['m', 'Heute ist ___ Wetter!', ['schönes', 'schöne', 'schöner', 'schönen'], 'Tanpa artikel, das Wetter Nominativ → -es'],
        ['o', 'Ich wünsche dir einen schönen Tag.', 'Semoga harimu menyenangkan.', 'einen + Akk maskulin → -en']
      ]
    },
    {
      id: 'b1-3',
      title: 'Relativsätze',
      sub: 'Anak kalimat dengan der, die, das, dem, denen …',
      monster: ['Relativ-Fuchs', '🦊'],
      theory: `
        <p><b>Relativsatz</b> menjelaskan kata benda ("yang …"). Kata kerja terkonjugasi di <b>akhir</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>mask.</th><th>fem.</th><th>neutr.</th><th>Plural</th></tr>
          <tr><td>Nom</td><td>der</td><td>die</td><td>das</td><td>die</td></tr>
          <tr><td>Akk</td><td>den</td><td>die</td><td>das</td><td>die</td></tr>
          <tr><td>Dat</td><td>dem</td><td>der</td><td>dem</td><td><span class="de">denen</span></td></tr>
          <tr><td>Gen</td><td><span class="de">dessen</span></td><td><span class="de">deren</span></td><td><span class="de">dessen</span></td><td><span class="de">deren</span></td></tr>
        </table></div>
        <h3>Cara memilih</h3>
        <ol>
          <li><b>Jenis & jumlah</b> → ikut kata benda yang dijelaskan.</li>
          <li><b>Kasus</b> → ikut fungsinya <u>di dalam</u> anak kalimat.</li>
        </ol>
        <p><i>Der Film, <b>den</b> ich gesehen habe, …</i> → der Film (mask.), ich sehe <u>ihn</u> (Akk) → <b>den</b>.</p>
        <div class="tip">💡 Preposisi berada di depan kata relatif: <i>die Freunde, <b>mit denen</b> ich reise</i>.<br>Setelah <i>alles, etwas, nichts, das</i> → <b>was</b>. Untuk tempat → bisa <b>wo</b>.</div>`,
      examples: [
        ['Das ist der Mann, der neben mir wohnt.', 'Itu pria yang tinggal di sebelah saya.'],
        ['Das Buch, das ich lese, ist spannend.', 'Buku yang saya baca menegangkan.'],
        ['Die Frau, der ich helfe, ist sehr alt.', 'Wanita yang saya bantu sudah sangat tua.'],
        ['Die Kinder, mit denen ich spiele, sind nett.', 'Anak-anak yang bermain dengan saya baik hati.'],
        ['Alles, was du sagst, stimmt.', 'Semua yang kamu katakan benar.']
      ],
      ex: [
        ['m', 'Der Film, ___ wir gestern gesehen haben, war super.', ['den', 'der', 'dem', 'das'], 'der Film (mask.), wir sahen ihn (Akk) → den'],
        ['m', 'Die Kollegin, ___ ich oft helfe, heißt Sara.', ['der', 'die', 'den', 'denen'], 'helfen + Dativ; feminin → der'],
        ['m', 'Das sind die Freunde, mit ___ ich reise.', ['denen', 'den', 'die', 'dem'], 'mit + Dativ Plural → denen'],
        ['f', 'Das ist das Haus, in ___ ich geboren bin.', ['dem'], 'in + Dativ (Wo?), das Haus → dem'],
        ['m', 'Das ist der Autor, ___ Bücher sehr bekannt sind.', ['dessen', 'deren', 'dem', 'den'], 'Milik der Autor (maskulin) → dessen'],
        ['m', 'Das ist alles, ___ ich weiß.', ['was', 'das', 'dass', 'wo'], 'Setelah alles → was'],
        ['o', 'Die Stadt, in der ich wohne, ist sehr schön.', 'Kota tempat saya tinggal sangat indah.', 'Relativsatz dengan preposisi; kata kerja di akhir']
      ]
    },
    {
      id: 'b1-4',
      title: 'Konjunktiv II',
      sub: 'Pengandaian, harapan, sopan santun, saran',
      monster: ['Wunsch-Fee', '🧚'],
      theory: `
        <p><b>Konjunktiv II</b> dipakai untuk hal yang <b>tidak nyata</b> (pengandaian), <b>harapan</b>, <b>permintaan sopan</b>, dan <b>saran</b>.</p>
        <h3>Bentuk</h3>
        <ul>
          <li>Kebanyakan kata kerja: <b>würde + Infinitiv</b> → <i>Ich würde reisen.</i></li>
          <li>Bentuk khusus yang wajib dihafal:</li>
        </ul>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Infinitiv</th><th>ich / er</th><th>du</th><th>wir / sie</th></tr>
          <tr><td>sein</td><td><span class="de">wäre</span></td><td>wärst</td><td>wären</td></tr>
          <tr><td>haben</td><td><span class="de">hätte</span></td><td>hättest</td><td>hätten</td></tr>
          <tr><td>können</td><td>könnte</td><td>könntest</td><td>könnten</td></tr>
          <tr><td>müssen</td><td>müsste</td><td>müsstest</td><td>müssten</td></tr>
          <tr><td>sollen</td><td>sollte</td><td>solltest</td><td>sollten</td></tr>
          <tr><td>dürfen</td><td>dürfte</td><td>dürftest</td><td>dürften</td></tr>
        </table></div>
        <h3>Penggunaan</h3>
        <ul>
          <li>Pengandaian: <i>Wenn ich reich <b>wäre</b>, <b>würde</b> ich um die Welt reisen.</i></li>
          <li>Harapan: <i>Ich <b>hätte</b> gern mehr Zeit.</i></li>
          <li>Sopan: <i><b>Könnten</b> Sie mir bitte helfen?</i></li>
          <li>Saran: <i>Du <b>solltest</b> mehr schlafen. An deiner Stelle <b>würde</b> ich …</i></li>
        </ul>`,
      examples: [
        ['Wenn ich reich wäre, würde ich ein Haus kaufen.', 'Kalau aku kaya, aku akan membeli rumah.'],
        ['Ich hätte gern einen Tee.', 'Saya mau teh (sopan).'],
        ['Könnten Sie das bitte wiederholen?', 'Bisakah Anda mengulanginya?'],
        ['Du solltest mehr Wasser trinken.', 'Sebaiknya kamu minum lebih banyak air.'],
        ['An deiner Stelle würde ich zum Arzt gehen.', 'Kalau aku jadi kamu, aku akan ke dokter.']
      ],
      ex: [
        ['m', 'Wenn ich Zeit ___, würde ich dich besuchen.', ['hätte', 'habe', 'hatte', 'hat'], 'Pengandaian (tidak nyata) → hätte'],
        ['m', '___ Sie mir bitte das Salz geben? (paling sopan)', ['Könnten', 'Können', 'Konnten', 'Kannst'], 'Permintaan sopan → Konjunktiv II: Könnten'],
        ['m', 'An deiner Stelle ___ ich zum Arzt gehen.', ['würde', 'werde', 'wurde', 'wäre'], 'Saran → würde + Infinitiv'],
        ['f', 'Wenn ich ein Vogel ___, könnte ich fliegen. (sein)', ['wäre'], 'sein → wäre'],
        ['f', 'Ich ___ gern einen Tee. (haben, sopan)', ['hätte'], 'Ich hätte gern … = saya mau … (sopan)'],
        ['m', 'Du ___ mehr schlafen. (saran)', ['solltest', 'sollst', 'sollest', 'solltet'], 'Saran → du solltest'],
        ['o', 'Wenn ich reich wäre, würde ich um die Welt reisen.', 'Kalau aku kaya, aku akan keliling dunia.', 'Pengandaian: wenn … wäre, würde … Infinitiv']
      ]
    },
    {
      id: 'b1-5',
      title: 'Passiv (Präsens & Präteritum)',
      sub: 'Kalimat pasif: werden + Partizip II',
      monster: ['Passiv-Golem', '🗿'],
      theory: `
        <p>Kalimat pasif fokus pada <b>aksinya</b>, bukan pelakunya. Rumus: <b>werden</b> (posisi 2) + <b>Partizip II</b> (akhir).</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Zeit</th><th>Bentuk</th><th>Contoh</th></tr>
          <tr><td>Präsens</td><td>wird / werden + Partizip II</td><td>Das Haus <span class="de">wird</span> gebaut.</td></tr>
          <tr><td>Präteritum</td><td>wurde / wurden + Partizip II</td><td>Das Haus <span class="de">wurde</span> 1900 gebaut.</td></tr>
        </table></div>
        <h3>Aktiv → Passiv</h3>
        <p><i>Der Mechaniker repariert <b>das Auto</b>.</i> → <i><b>Das Auto</b> wird (vom Mechaniker) repariert.</i></p>
        <ul>
          <li>Objek Akkusativ menjadi <b>subjek (Nominativ)</b>: den Patienten → der Patient.</li>
          <li>Pelaku (opsional): <b>von + Dativ</b> (orang); <b>durch + Akk</b> (penyebab/sarana).</li>
          <li>Subjek "man" → hilang: <i>Man spricht hier Deutsch → Hier wird Deutsch gesprochen.</i></li>
        </ul>`,
      examples: [
        ['Das Auto wird repariert.', 'Mobil itu sedang diperbaiki.'],
        ['Hier wird Deutsch gesprochen.', 'Di sini digunakan bahasa Jerman.'],
        ['Das Haus wurde 1900 gebaut.', 'Rumah itu dibangun tahun 1900.'],
        ['Die Briefe werden von der Post gebracht.', 'Surat-surat itu diantar oleh kantor pos.'],
        ['Das Fenster wurde durch den Sturm zerstört.', 'Jendela itu rusak karena badai.']
      ],
      ex: [
        ['m', 'Die Fenster ___ jeden Monat geputzt.', ['werden', 'wird', 'wurde', 'sind'], 'Subjek jamak, Präsens → werden'],
        ['m', 'Das Brot ___ in der Bäckerei gebacken.', ['wird', 'werden', 'ist', 'hat'], 'Subjek tunggal, Präsens → wird'],
        ['m', 'Der Dieb ___ gestern von der Polizei gefasst.', ['wurde', 'wird', 'würde', 'war'], 'gestern → Präteritum: wurde'],
        ['f', 'Das Museum wurde 1890 ___. (bauen)', ['gebaut'], 'Partizip II dari bauen → gebaut'],
        ['m', 'Bentuk pasif dari "Der Arzt untersucht den Patienten." adalah …', ['Der Patient wird vom Arzt untersucht.', 'Den Patienten wird vom Arzt untersucht.', 'Der Patient wurde den Arzt untersucht.', 'Der Patient wird von den Arzt untersucht.'], 'Objek Akk → subjek Nominativ; von + Dativ (vom Arzt)'],
        ['f', 'Die Briefe ___ gestern verschickt. (werden, Präteritum)', ['wurden'], 'Plural Präteritum → wurden'],
        ['o', 'In Österreich wird Deutsch gesprochen.', 'Di Austria digunakan bahasa Jerman.', 'werden + Partizip II']
      ]
    },
    {
      id: 'b1-6',
      title: 'Infinitiv mit zu, um … zu, damit',
      sub: 'Konstruksi infinitif dan kalimat tujuan',
      monster: ['Zu-Zauberer', '🧙'],
      theory: `
        <h3>1. zu + Infinitiv</h3>
        <p>Setelah kata kerja/ungkapan tertentu: <i>versuchen, vergessen, anfangen, aufhören, hoffen, vorhaben, Lust haben, Zeit haben, Es ist wichtig/schön/schwer …</i></p>
        <p><i>Ich habe vergessen, dich <b>anzurufen</b>.</i> → kata kerja pisah: <b>an-zu-rufen</b>.</p>
        <div class="warn">⚠️ Setelah <b>Modalverben</b> TIDAK pakai zu: <i>Ich kann kommen.</i></div>
        <h3>2. Tujuan: um … zu vs. damit</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Konstruksi</th><th>Kapan</th><th>Contoh</th></tr>
          <tr><td><b>um … zu</b></td><td>subjek <u>sama</u></td><td>Ich lerne Deutsch, um in Berlin zu studieren.</td></tr>
          <tr><td><b>damit</b></td><td>subjek <u>berbeda</u> (atau sama)</td><td>Ich spreche langsam, damit du mich verstehst.</td></tr>
        </table></div>
        <h3>3. ohne … zu, (an)statt … zu</h3>
        <p><i>Er ging, <b>ohne</b> ein Wort <b>zu sagen</b>.</i> · <i><b>Statt</b> zu lernen, spielt er.</i></p>`,
      examples: [
        ['Ich versuche, jeden Tag Deutsch zu sprechen.', 'Saya berusaha berbicara bahasa Jerman setiap hari.'],
        ['Es ist wichtig, regelmäßig zu üben.', 'Penting untuk berlatih secara teratur.'],
        ['Sie spart Geld, um ein Auto zu kaufen.', 'Dia menabung untuk membeli mobil.'],
        ['Ich erkläre es langsam, damit alle es verstehen.', 'Saya menjelaskannya pelan agar semua mengerti.'],
        ['Er ging, ohne ein Wort zu sagen.', 'Dia pergi tanpa berkata sepatah kata pun.']
      ],
      ex: [
        ['m', 'Ich habe vergessen, dich ___.', ['anzurufen', 'zu anrufen', 'anrufen', 'anzurufend'], 'Kata kerja pisah: an-zu-rufen'],
        ['m', 'Er lernt Deutsch, ___ in Wien zu arbeiten.', ['um', 'damit', 'dass', 'für'], 'Tujuan, subjek sama → um … zu'],
        ['m', 'Ich erkläre es dir noch einmal, ___ du es verstehst.', ['damit', 'um', 'ohne', 'statt'], 'Subjek berbeda (ich/du) → damit'],
        ['m', 'Wir können heute leider nicht ___.', ['kommen', 'zu kommen', 'um kommen', 'kommend'], 'Setelah Modalverb → tanpa zu'],
        ['f', 'Hast du Lust, ins Kino zu ___? (gehen)', ['gehen'], 'Lust haben + zu + Infinitiv'],
        ['f', 'Es ist schwer, früh ___. (aufstehen, + zu)', ['aufzustehen'], 'Kata kerja pisah: auf-zu-stehen'],
        ['o', 'Sie ging, ohne ein Wort zu sagen.', 'Dia pergi tanpa berkata sepatah kata pun.', 'ohne … zu + Infinitiv']
      ]
    },
    {
      id: 'b1-7',
      title: 'Temporale Nebensätze & Plusquamperfekt',
      sub: 'als, wenn, während, bevor, nachdem, seit, bis',
      monster: ['Zeit-Uhu', '⏳'],
      theory: `
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Konjungsi</th><th>Arti</th><th>Contoh</th></tr>
          <tr><td><b>als</b></td><td>ketika (satu kali, lampau)</td><td>Als ich zehn war, zogen wir um.</td></tr>
          <tr><td><b>wenn</b></td><td>ketika/setiap kali (berulang, atau sekarang/masa depan)</td><td>Immer wenn es regnete, las ich.</td></tr>
          <tr><td><b>während</b></td><td>sementara/selama</td><td>Während ich koche, hört sie Musik.</td></tr>
          <tr><td><b>bevor</b></td><td>sebelum</td><td>Bevor du gehst, mach das Licht aus.</td></tr>
          <tr><td><b>nachdem</b></td><td>setelah</td><td>Nachdem er gegessen hatte, ging er.</td></tr>
          <tr><td><b>seit(dem)</b></td><td>sejak</td><td>Seit ich hier wohne, bin ich glücklich.</td></tr>
          <tr><td><b>bis</b></td><td>sampai</td><td>Warte, bis ich komme.</td></tr>
        </table></div>
        <h3>Plusquamperfekt (lampau dari lampau)</h3>
        <p><b>hatte / war + Partizip II</b>. Dipakai untuk kejadian yang terjadi <b>sebelum</b> kejadian lampau lain, terutama setelah <b>nachdem</b>:</p>
        <p><i>Nachdem wir <b>gegessen hatten</b>, <b>gingen</b> wir spazieren.</i></p>
        <div class="warn">⚠️ <b>wann</b> hanya untuk pertanyaan ("kapan?"): <i>Wann kommst du?</i></div>`,
      examples: [
        ['Als ich zehn war, zog ich nach Surabaya.', 'Ketika saya berumur sepuluh tahun, saya pindah ke Surabaya.'],
        ['Immer wenn es regnet, bleibe ich zu Hause.', 'Setiap kali hujan, saya di rumah.'],
        ['Nachdem er gegessen hatte, ging er spazieren.', 'Setelah makan, dia jalan-jalan.'],
        ['Bevor du gehst, mach bitte das Licht aus.', 'Sebelum kamu pergi, tolong matikan lampu.'],
        ['Während ich koche, hört sie Musik.', 'Sementara saya memasak, dia mendengarkan musik.']
      ],
      ex: [
        ['m', '___ ich klein war, wohnten wir in Medan.', ['Als', 'Wenn', 'Wann', 'Ob'], 'Satu periode di masa lalu → als'],
        ['m', 'Immer ___ ich Oma besuchte, backte sie Kuchen.', ['wenn', 'als', 'wann', 'nachdem'], 'Berulang di masa lalu (immer) → wenn'],
        ['m', 'Nachdem wir gegessen ___, gingen wir spazieren.', ['hatten', 'haben', 'hätten', 'waren'], 'nachdem + Plusquamperfekt: hatten gegessen'],
        ['f', 'Nachdem sie angekommen ___, rief sie mich an. (sein)', ['war'], 'ankommen → Perfekt dengan sein → Plusquamperfekt: war angekommen'],
        ['m', '___ ich schlafe, lese ich immer ein Buch.', ['Bevor', 'Nachdem', 'Als', 'Seit'], 'Sebelum tidur → bevor'],
        ['m', '___ fährst du in den Urlaub? – Im August.', ['Wann', 'Als', 'Wenn', 'Ob'], 'Pertanyaan "kapan?" → wann'],
        ['o', 'Während ich arbeite, höre ich Musik.', 'Saat bekerja, saya mendengarkan musik.', 'Anak kalimat di depan → induk kalimat dimulai kata kerja']
      ]
    },
    {
      id: 'b1-8',
      title: 'Futur I',
      sub: 'Masa depan, rencana, prediksi: werden + Infinitiv',
      monster: ['Zukunfts-Stern', '🌠'],
      theory: `
        <p><b>Futur I</b> dibentuk dengan <b>werden</b> (posisi 2) + <b>Infinitiv</b> (akhir).</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>ich</th><th>du</th><th>er/sie/es</th><th>wir</th><th>ihr</th><th>sie/Sie</th></tr>
          <tr><td>werde</td><td><span class="de">wirst</span></td><td><span class="de">wird</span></td><td>werden</td><td>werdet</td><td>werden</td></tr>
        </table></div>
        <h3>Kapan dipakai?</h3>
        <ul>
          <li><b>Rencana / janji</b>: <i>Ich <b>werde</b> dich morgen <b>anrufen</b>.</i></li>
          <li><b>Prediksi</b>: <i>Morgen <b>wird</b> es <b>regnen</b>.</i></li>
          <li><b>Dugaan tentang sekarang</b> (sering dengan <i>wohl</i>): <i>Er <b>wird</b> wohl noch <b>schlafen</b>.</i></li>
        </ul>
        <div class="tip">💡 Dalam percakapan, masa depan sering cukup memakai <b>Präsens + keterangan waktu</b>: <i>Morgen fahre ich nach Berlin.</i></div>
        <p>Dalam anak kalimat, <i>werden</i> berada di paling akhir: <i>…, dass er morgen kommen <b>wird</b>.</i></p>`,
      examples: [
        ['Ich werde dich morgen anrufen.', 'Aku akan meneleponmu besok.'],
        ['Morgen wird es regnen.', 'Besok akan hujan.'],
        ['Wirst du am Wochenende arbeiten?', 'Apakah kamu akan bekerja di akhir pekan?'],
        ['Er wird wohl noch im Büro sein.', 'Dia mungkin masih di kantor.'],
        ['Ich glaube, dass sie die Prüfung bestehen wird.', 'Saya yakin dia akan lulus ujian.']
      ],
      ex: [
        ['m', 'Morgen ___ es regnen.', ['wird', 'werdet', 'wirst', 'wurde'], 'es → wird'],
        ['m', 'Du ___ das schaffen, da bin ich sicher.', ['wirst', 'wird', 'werdest', 'werdet'], 'du → wirst'],
        ['f', 'Wir ___ im Sommer nach Japan fliegen. (werden)', ['werden'], 'wir → werden'],
        ['m', 'Ich glaube, dass er morgen ___.', ['kommen wird', 'wird kommen', 'kommt werden', 'werden kommt'], 'Anak kalimat → werden di paling akhir'],
        ['m', 'Er geht nicht ans Telefon. Er ___ wohl noch schlafen.', ['wird', 'wurde', 'ist', 'hat'], 'Dugaan tentang sekarang → wird wohl + Infinitiv'],
        ['f', 'Ihr ___ es nicht bereuen! (werden)', ['werdet'], 'ihr → werdet'],
        ['o', 'Nächstes Jahr werde ich in Deutschland studieren.', 'Tahun depan saya akan kuliah di Jerman.', 'werden di posisi 2, Infinitiv di akhir']
      ]
    }
  ]
});
