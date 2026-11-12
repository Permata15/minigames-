/* Level B2 – Alpenburg (Kastil Pegunungan Alpen) */
window.GRAMMATIK_LEVELS = window.GRAMMATIK_LEVELS || [];
window.GRAMMATIK_LEVELS.push({
  id: 'B2',
  title: 'Alpenburg',
  subtitle: 'Kastil Pegunungan Alpen',
  icon: '🏰',
  color: '#7b5cd6',
  intro: 'Jalan mendaki membawamu ke <b>Alpenburg</b>, kastil megah di puncak Alpen. Di sini para bangsawan berbicara dengan kalimat yang rumit dan elegan. <b>Der Konnektoren-Riese</b> menjaga gerbangnya — buktikan bahwa kamu menguasai Passiv, Konjunktiv II lampau, dan konektor tingkat lanjut!',
  boss: ['Der Konnektoren-Riese', '👹'],
  topics: [
    {
      id: 'b2-1',
      title: 'Passiv: Modalverben, Perfekt & Zustandspassiv',
      sub: 'muss gemacht werden · ist gemacht worden · ist geöffnet',
      monster: ['Burggeist', '🧟'],
      theory: `
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Bentuk</th><th>Rumus</th><th>Contoh</th></tr>
          <tr><td>Passiv + Modalverb</td><td>Modal … Partizip II + <b>werden</b></td><td>Das Auto <b>muss</b> repariert <b>werden</b>.</td></tr>
          <tr><td>Modal Präteritum</td><td>konnte/musste … Partizip II + werden</td><td>Der Fehler <b>konnte</b> nicht gefunden <b>werden</b>.</td></tr>
          <tr><td>Perfekt Passiv</td><td><b>sein</b> … Partizip II + <b>worden</b></td><td>Das Haus <b>ist</b> renoviert <b>worden</b>.</td></tr>
          <tr><td>Plusquamperfekt Passiv</td><td><b>war</b> … Partizip II + <b>worden</b></td><td>Die Brücke <b>war</b> zerstört <b>worden</b>.</td></tr>
          <tr><td>Zustandspassiv (hasil/keadaan)</td><td><b>sein</b> + Partizip II</td><td>Die Tür <b>ist</b> geschlossen.</td></tr>
        </table></div>
        <div class="warn">⚠️ Di Passiv Perfekt dipakai <b>worden</b>, bukan <i>geworden</i>. (<i>geworden</i> = "menjadi": Er ist Arzt geworden.)</div>
        <div class="tip">💡 <b>Vorgangspassiv</b> (werden) = prosesnya: <i>Die Tür wird geschlossen</i> (sedang ditutup).<br><b>Zustandspassiv</b> (sein) = hasilnya: <i>Die Tür ist geschlossen</i> (sudah tertutup).</div>
        <p>Dalam anak kalimat: <i>…, dass das Problem gelöst werden <b>muss</b>.</i> / <i>…, dass das Konzert abgesagt worden <b>ist</b>.</i></p>`,
      examples: [
        ['Die Rechnung muss bis Freitag bezahlt werden.', 'Tagihan harus dibayar sampai hari Jumat.'],
        ['Das Paket ist gestern geliefert worden.', 'Paket itu sudah diantar kemarin.'],
        ['Das Geschäft ist sonntags geschlossen.', 'Toko itu tutup setiap hari Minggu.'],
        ['Der Fehler konnte nicht gefunden werden.', 'Kesalahan itu tidak dapat ditemukan.'],
        ['Ich habe gehört, dass das Konzert abgesagt worden ist.', 'Saya dengar konsernya sudah dibatalkan.']
      ],
      ex: [
        ['m', 'Die Rechnung muss bis Freitag bezahlt ___.', ['werden', 'worden', 'wird', 'sein'], 'Modalverb + Partizip II + werden'],
        ['m', 'Das Paket ist gestern geliefert ___.', ['worden', 'geworden', 'werden', 'wurde'], 'Perfekt Passiv → ist … worden'],
        ['m', 'Keine Sorge, die Tür ist schon ___.', ['geschlossen', 'schließen', 'geschlossen werden', 'schließt'], 'Keadaan/hasil → Zustandspassiv: sein + Partizip II'],
        ['f', 'Der Fehler konnte nicht gefunden ___.', ['werden'], 'Modal (Präteritum) + Partizip II + werden'],
        ['f', 'Die Brücke war 1950 zerstört ___. (Plusquamperfekt Passiv)', ['worden'], 'war + Partizip II + worden'],
        ['m', 'Ich habe gehört, dass das Konzert abgesagt ___.', ['worden ist', 'ist worden', 'geworden ist', 'wird worden'], 'Anak kalimat: … Partizip II + worden + ist (kata kerja terkonjugasi di akhir)'],
        ['o', 'Das Problem muss sofort gelöst werden.', 'Masalah itu harus segera diselesaikan.', 'Modalverb + Passiv-Infinitiv (gelöst werden)']
      ]
    },
    {
      id: 'b2-2',
      title: 'Konjunktiv II der Vergangenheit',
      sub: 'Pengandaian di masa lalu: hätte gemacht, wäre gegangen',
      monster: ['Reue-Gespenst', '💭'],
      theory: `
        <p>Untuk hal yang <b>tidak terjadi di masa lalu</b> (penyesalan, pengandaian lampau):</p>
        <p style="text-align:center"><b>hätte / wäre + Partizip II</b></p>
        <p>Pilih <i>hätte</i> atau <i>wäre</i> sama seperti di Perfekt (gerak/perubahan keadaan → wäre).</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Penggunaan</th><th>Contoh</th></tr>
          <tr><td>Pengandaian lampau</td><td>Wenn ich das <b>gewusst hätte</b>, <b>wäre</b> ich <b>gekommen</b>.</td></tr>
          <tr><td>Penyesalan / harapan</td><td><b>Hätte</b> ich doch mehr <b>gelernt</b>!</td></tr>
          <tr><td>Kritik / saran lampau</td><td>Du <b>hättest</b> mich anrufen <b>sollen</b>.</td></tr>
          <tr><td>Perbandingan tak nyata</td><td>Er tut so, <b>als ob</b> er nichts <b>gehört hätte</b>.</td></tr>
        </table></div>
        <div class="tip">💡 Dengan Modalverb muncul <b>Doppelinfinitiv</b>: <i>hätte … machen <b>müssen/sollen/können</b></i> (bukan "gemusst").</div>
        <p><b>als ob / als wenn</b> + Konjunktiv II = "seolah-olah". Bisa juga: <i>Er tut so, <b>als hätte</b> er nichts gehört.</i></p>`,
      examples: [
        ['Wenn ich das gewusst hätte, wäre ich gekommen.', 'Kalau saya tahu itu, saya pasti datang.'],
        ['Hätte ich doch mehr gelernt!', 'Seandainya saja saya belajar lebih banyak!'],
        ['Du hättest mich anrufen sollen.', 'Seharusnya kamu meneleponku.'],
        ['Er tut so, als ob er nichts gehört hätte.', 'Dia berlagak seolah-olah tidak mendengar apa-apa.'],
        ['Ohne deine Hilfe hätte ich es nicht geschafft.', 'Tanpa bantuanmu, aku tidak akan berhasil.']
      ],
      ex: [
        ['m', 'Wenn du früher gekommen ___, hättest du ihn getroffen.', ['wärst', 'hättest', 'würdest', 'bist'], 'kommen → Perfekt dengan sein → wärst gekommen'],
        ['m', 'Ich ___ fast den Zug verpasst.', ['hätte', 'wäre', 'würde', 'wurde'], 'verpassen → haben → hätte verpasst (hampir saja)'],
        ['m', 'Du hättest mir das früher sagen ___.', ['sollen', 'gesollt', 'sollst', 'zu sollen'], 'Doppelinfinitiv: hätte … sagen sollen'],
        ['f', 'Wenn ich das gewusst ___, hätte ich dir geholfen. (haben)', ['hätte'], 'wissen → haben → hätte gewusst'],
        ['f', 'Er tut so, als ob er krank ___. (sein, sekarang)', ['wäre'], 'als ob + Konjunktiv II'],
        ['m', '___ ich doch früher aufgestanden!', ['Wäre', 'Hätte', 'Würde', 'War'], 'aufstehen → sein → Wäre ich doch … aufgestanden!'],
        ['o', 'Ohne deine Hilfe hätte ich die Prüfung nicht bestanden.', 'Tanpa bantuanmu, aku tidak akan lulus ujian.', 'hätte + Partizip II']
      ]
    },
    {
      id: 'b2-3',
      title: 'Zweiteilige Konnektoren',
      sub: 'sowohl … als auch, weder … noch, je … desto …',
      monster: ['Ketten-Basilisk', '🦎'],
      theory: `
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Konektor</th><th>Arti</th><th>Contoh</th></tr>
          <tr><td><b>sowohl … als auch</b></td><td>baik … maupun</td><td>Er spricht sowohl Englisch als auch Spanisch.</td></tr>
          <tr><td><b>weder … noch</b></td><td>bukan … dan bukan</td><td>Ich habe weder Zeit noch Geld.</td></tr>
          <tr><td><b>entweder … oder</b></td><td>atau … atau</td><td>Entweder du kommst mit, oder du bleibst hier.</td></tr>
          <tr><td><b>nicht nur … sondern auch</b></td><td>tidak hanya … tetapi juga</td><td>Sie ist nicht nur klug, sondern auch nett.</td></tr>
          <tr><td><b>zwar … aber</b></td><td>memang … tetapi</td><td>Das Auto ist zwar alt, aber zuverlässig.</td></tr>
          <tr><td><b>je … desto / umso</b></td><td>semakin … semakin</td><td>Je mehr ich lerne, desto mehr weiß ich.</td></tr>
        </table></div>
        <h3>Susunan kata "je … desto"</h3>
        <p><b>Je</b> + Komparativ + … + kata kerja <b>di akhir</b>, <b>desto</b> + Komparativ + <b>kata kerja</b> + subjek …</p>
        <p><i>Je länger ich warte, desto nervöser werde ich.</i></p>`,
      examples: [
        ['Er spricht sowohl Englisch als auch Spanisch.', 'Dia berbicara baik bahasa Inggris maupun Spanyol.'],
        ['Ich habe weder Zeit noch Lust.', 'Saya tidak punya waktu dan tidak berminat.'],
        ['Wir fahren entweder ans Meer oder in die Berge.', 'Kami pergi ke laut atau ke gunung.'],
        ['Je mehr du übst, desto besser sprichst du.', 'Semakin banyak berlatih, semakin baik bicaramu.'],
        ['Die Wohnung ist zwar klein, aber sehr gemütlich.', 'Apartemen itu memang kecil, tapi sangat nyaman.']
      ],
      ex: [
        ['m', 'Er spricht ___ Englisch als auch Spanisch.', ['sowohl', 'weder', 'entweder', 'zwar'], 'sowohl … als auch'],
        ['m', 'Ich habe ___ Zeit noch Geld.', ['weder', 'sowohl', 'entweder', 'nicht'], 'weder … noch'],
        ['m', 'Je mehr du übst, ___ besser sprichst du.', ['desto', 'als', 'so', 'wie'], 'je … desto'],
        ['m', 'Die Wohnung ist ___ klein, aber sehr gemütlich.', ['zwar', 'sowohl', 'weder', 'je'], 'zwar … aber'],
        ['f', 'Wir fahren ___ ans Meer oder in die Berge. (pilihan)', ['entweder'], 'entweder … oder'],
        ['o', 'Je länger ich warte, desto nervöser werde ich.', 'Makin lama menunggu, makin gugup saya.', 'je + … kata kerja di akhir, desto + Komparativ + kata kerja + subjek'],
        ['o', 'Sie ist nicht nur klug, sondern auch sehr hilfsbereit.', 'Dia tidak hanya pintar, tetapi juga sangat suka menolong.', 'nicht nur … sondern auch']
      ]
    },
    {
      id: 'b2-4',
      title: 'Genitiv & Präpositionen mit Genitiv',
      sub: 'des Mannes · wegen, trotz, während, aufgrund …',
      monster: ['Genitiv-Greif', '🦖'],
      theory: `
        <p><b>Genitiv</b> menyatakan kepemilikan ("milik …") dan dipakai setelah preposisi tertentu, terutama dalam bahasa tulis/formal.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>mask.</th><th>fem.</th><th>neutr.</th><th>Plural</th></tr>
          <tr><td>tertentu</td><td><span class="de">des</span> Mann<b>es</b></td><td><span class="de">der</span> Frau</td><td><span class="de">des</span> Kind<b>es</b></td><td><span class="de">der</span> Kinder</td></tr>
          <tr><td>tak tentu</td><td>eines Mann<b>es</b></td><td>einer Frau</td><td>eines Kind<b>es</b></td><td>–</td></tr>
          <tr><td>+ kata sifat</td><td>des alt<b>en</b> Mannes</td><td>der alt<b>en</b> Frau</td><td>des klein<b>en</b> Kindes</td><td>der klein<b>en</b> Kinder</td></tr>
        </table></div>
        <p>Maskulin & neutral mendapat <b>-s</b> atau <b>-es</b> (kata 1 suku kata / berakhiran -s, -ß, -z: <i>des Hauses</i>).</p>
        <h3>Preposisi + Genitiv</h3>
        <p><b>wegen</b> (karena), <b>trotz</b> (meskipun), <b>während</b> (selama), <b>(an)statt</b> (alih-alih), <b>innerhalb / außerhalb</b> (di dalam / di luar), <b>aufgrund</b> (berdasarkan/karena), <b>infolge</b> (akibat), <b>anlässlich</b> (dalam rangka), <b>bezüglich</b> (mengenai)</p>
        <div class="tip">💡 Dalam bahasa lisan sering dipakai Dativ (<i>wegen dem Wetter</i>), tetapi dalam ujian dan tulisan gunakan <b>Genitiv</b>: <i>wegen des Wetters</i>.</div>`,
      examples: [
        ['Das Auto meines Vaters ist rot.', 'Mobil ayah saya berwarna merah.'],
        ['Wegen des schlechten Wetters bleiben wir zu Hause.', 'Karena cuaca buruk, kami tinggal di rumah.'],
        ['Trotz des Regens gingen wir spazieren.', 'Meskipun hujan, kami tetap jalan-jalan.'],
        ['Während der Ferien arbeitet sie in einem Café.', 'Selama liburan dia bekerja di kafe.'],
        ['Innerhalb einer Woche müssen Sie antworten.', 'Anda harus menjawab dalam waktu satu minggu.']
      ],
      ex: [
        ['m', '___ des Staus kam ich zu spät.', ['Wegen', 'Trotz', 'Statt', 'Innerhalb'], 'Sebab → wegen'],
        ['m', 'Trotz ___ Regens spielten die Kinder draußen.', ['des', 'dem', 'den', 'der'], 'der Regen → Genitiv: des Regens'],
        ['m', 'Während ___ Sitzung darf man nicht telefonieren.', ['der', 'die', 'dem', 'des'], 'die Sitzung → Genitiv: der Sitzung'],
        ['f', 'Das ist das Haus meines ___. (der Onkel)', ['Onkels'], 'Genitiv maskulin → -s: meines Onkels'],
        ['f', 'Aufgrund ___ starken Windes wurde der Flug gestrichen. (Artikel)', ['des'], 'der Wind → des starken Windes'],
        ['m', 'Das Ende ___ Films war überraschend.', ['des', 'der', 'dem', 'den'], 'der Film → Genitiv: des Films'],
        ['o', 'Statt eines Briefes schrieb er eine E-Mail.', 'Alih-alih menulis surat, dia menulis email.', 'statt + Genitiv']
      ]
    },
    {
      id: 'b2-5',
      title: 'Verben mit Präpositionen & da-/wo-Wörter',
      sub: 'warten auf, denken an · darauf, woran …',
      monster: ['Präpositions-Yeti', '⛄'],
      theory: `
        <p>Banyak kata kerja punya <b>preposisi tetap</b> dan kasus tertentu. Hafalkan sebagai satu paket!</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>+ Akkusativ</th><th>+ Dativ</th></tr>
          <tr><td>warten <b>auf</b> · denken <b>an</b> · sich freuen <b>auf</b> (akan datang) · sich freuen <b>über</b> (sudah terjadi) · sich interessieren <b>für</b> · sich erinnern <b>an</b> · sich ärgern <b>über</b> · sich kümmern <b>um</b></td>
              <td>Angst haben <b>vor</b> · träumen <b>von</b> · teilnehmen <b>an</b> · abhängen <b>von</b> · sich beschäftigen <b>mit</b> · fragen <b>nach</b> · sich verabschieden <b>von</b></td></tr>
        </table></div>
        <h3>da- dan wo- untuk benda/hal</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>Benda / hal</th><th>Orang</th></tr>
          <tr><td>Pertanyaan</td><td><span class="de">Worauf</span> wartest du?</td><td><span class="de">Auf wen</span> wartest du?</td></tr>
          <tr><td>Jawaban</td><td>Ich warte <span class="de">darauf</span>.</td><td>Ich warte <span class="de">auf ihn</span>.</td></tr>
        </table></div>
        <div class="tip">💡 Jika preposisinya diawali vokal, tambahkan <b>-r-</b>: da<b>r</b>auf, wo<b>r</b>an, da<b>r</b>über. Jika konsonan: dafür, damit, wovon.</div>`,
      examples: [
        ['Ich warte schon lange auf den Bus.', 'Saya sudah lama menunggu bus.'],
        ['Sie hat Angst vor Spinnen.', 'Dia takut laba-laba.'],
        ['Woran denkst du? – An meinen Urlaub.', 'Kamu memikirkan apa? – Liburanku.'],
        ['Ich freue mich schon darauf.', 'Saya sudah menantikannya.'],
        ['Er nimmt an einem Deutschkurs teil.', 'Dia mengikuti kursus bahasa Jerman.']
      ],
      ex: [
        ['m', 'Ich warte schon seit einer Stunde ___ den Bus.', ['auf', 'für', 'an', 'über'], 'warten auf + Akkusativ'],
        ['m', 'Sie hat Angst ___ Hunden.', ['vor', 'von', 'über', 'für'], 'Angst haben vor + Dativ'],
        ['m', '___ denkst du? – An meinen Urlaub.', ['Woran', 'Worauf', 'Wovon', 'Wofür'], 'denken an + hal → woran'],
        ['m', 'Freust du dich auf die Party? – Ja, ich freue mich sehr ___.', ['darauf', 'dafür', 'daran', 'auf es'], 'Hal/benda → da(r) + preposisi: darauf'],
        ['f', 'Er interessiert sich sehr ___ Politik.', ['für'], 'sich interessieren für + Akkusativ'],
        ['f', 'Hast du an die Blumen gedacht? – Ja, ich habe ___ gedacht.', ['daran'], 'denken an + hal → daran'],
        ['o', 'Ich erinnere mich gern an meine Kindheit.', 'Saya senang mengenang masa kecil saya.', 'sich erinnern an + Akkusativ']
      ]
    },
    {
      id: 'b2-6',
      title: 'Nomen-Verb-Verbindungen',
      sub: 'Ungkapan baku: eine Entscheidung treffen, in Kraft treten …',
      monster: ['Phrasen-Hydra', '🐲'],
      theory: `
        <p><b>Nomen-Verb-Verbindungen</b> (Funktionsverbgefüge) adalah pasangan kata benda + kata kerja yang tetap. Sangat umum dalam bahasa <b>formal, berita, dan ujian B2/C1</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Ungkapan</th><th>Makna (= kata kerja)</th></tr>
          <tr><td>eine Entscheidung <b>treffen</b></td><td>entscheiden (memutuskan)</td></tr>
          <tr><td>eine Frage <b>stellen</b></td><td>fragen (bertanya)</td></tr>
          <tr><td>einen Antrag <b>stellen</b></td><td>beantragen (mengajukan permohonan)</td></tr>
          <tr><td>Kritik <b>üben</b> an</td><td>kritisieren (mengkritik)</td></tr>
          <tr><td>eine Rolle <b>spielen</b></td><td>wichtig sein (berperan)</td></tr>
          <tr><td>zur Verfügung <b>stehen</b></td><td>verfügbar sein (tersedia)</td></tr>
          <tr><td>in Kraft <b>treten</b></td><td>gültig werden (mulai berlaku)</td></tr>
          <tr><td>Rücksicht <b>nehmen</b> auf</td><td>berücksichtigen (memperhatikan/tenggang rasa)</td></tr>
          <tr><td>Maßnahmen <b>ergreifen</b></td><td>handeln (mengambil tindakan)</td></tr>
          <tr><td>zum Ausdruck <b>bringen</b></td><td>ausdrücken (mengungkapkan)</td></tr>
          <tr><td>in Frage <b>kommen</b></td><td>möglich sein (memungkinkan/dipertimbangkan)</td></tr>
          <tr><td>Bescheid <b>geben/sagen</b></td><td>informieren (memberi kabar)</td></tr>
        </table></div>`,
      examples: [
        ['Wir müssen bald eine Entscheidung treffen.', 'Kita harus segera mengambil keputusan.'],
        ['Darf ich Ihnen eine Frage stellen?', 'Bolehkah saya bertanya kepada Anda?'],
        ['Das neue Gesetz tritt im Januar in Kraft.', 'Undang-undang baru mulai berlaku Januari.'],
        ['Geld spielt für ihn keine Rolle.', 'Uang tidak penting baginya.'],
        ['Sag mir bitte Bescheid, wenn du ankommst.', 'Tolong kabari aku kalau kamu sudah sampai.']
      ],
      ex: [
        ['m', 'Wir müssen bald eine Entscheidung ___.', ['treffen', 'machen', 'geben', 'stellen'], 'eine Entscheidung treffen'],
        ['m', 'Darf ich Ihnen eine Frage ___?', ['stellen', 'fragen', 'machen', 'legen'], 'eine Frage stellen'],
        ['m', 'Das Gesetz tritt im Januar in ___.', ['Kraft', 'Macht', 'Wirkung', 'Stärke'], 'in Kraft treten = mulai berlaku'],
        ['f', 'Geld spielt für ihn keine ___.', ['Rolle'], 'eine Rolle spielen'],
        ['m', 'Bitte nehmen Sie ___ auf die anderen Gäste.', ['Rücksicht', 'Achtung', 'Vorsicht', 'Sorge'], 'Rücksicht nehmen auf'],
        ['f', 'Der Raum steht Ihnen jederzeit zur ___.', ['Verfügung'], 'zur Verfügung stehen = tersedia'],
        ['o', 'Die Regierung muss sofort Maßnahmen ergreifen.', 'Pemerintah harus segera mengambil tindakan.', 'Maßnahmen ergreifen']
      ]
    },
    {
      id: 'b2-7',
      title: 'Futur II',
      sub: 'Akan sudah selesai & dugaan tentang masa lalu',
      monster: ['Chronos-Uhrwerk', '🕰️'],
      theory: `
        <p><b>Futur II</b> = <b>werden</b> + <b>Partizip II</b> + <b>haben / sein</b> (Infinitiv Perfekt di akhir).</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Penggunaan</th><th>Contoh</th></tr>
          <tr><td>Tindakan yang <b>sudah selesai</b> pada suatu titik di masa depan</td><td>Bis Freitag <b>werde</b> ich die Arbeit <b>beendet haben</b>.</td></tr>
          <tr><td><b>Dugaan</b> tentang sesuatu yang sudah terjadi (sering dengan <i>wohl, schon, bestimmt</i>)</td><td>Er <b>wird</b> wohl den Bus <b>verpasst haben</b>.</td></tr>
        </table></div>
        <div class="tip">💡 Pilih <b>haben</b> atau <b>sein</b> sama seperti di Perfekt: <i>gegessen haben</i>, tetapi <i>gegangen sein, angekommen sein</i>.</div>
        <p>Dalam anak kalimat: <i>…, dass er den Bus verpasst haben <b>wird</b>.</i></p>`,
      examples: [
        ['Bis Freitag werde ich die Arbeit beendet haben.', 'Sampai Jumat saya sudah akan menyelesaikan pekerjaan itu.'],
        ['In zwei Jahren wird sie ihr Studium abgeschlossen haben.', 'Dua tahun lagi dia sudah akan menamatkan kuliahnya.'],
        ['Er wird wohl den Bus verpasst haben.', 'Dia mungkin ketinggalan bus.'],
        ['Bis du kommst, werden wir schon gegessen haben.', 'Saat kamu datang, kami sudah akan selesai makan.'],
        ['Sie wird schon nach Hause gegangen sein.', 'Dia mungkin sudah pulang.']
      ],
      ex: [
        ['m', 'Bis morgen werde ich den Bericht ___.', ['geschrieben haben', 'schreiben haben', 'geschrieben sein', 'geschrieben werden'], 'Futur II: werden + Partizip II + haben'],
        ['m', 'Bis 20 Uhr ___ wir in Berlin angekommen sein.', ['werden', 'haben', 'sind', 'wurden'], 'Futur II: werden … angekommen sein'],
        ['f', 'Er ist nicht da. Er wird wohl schon nach Hause gegangen ___. (haben/sein)', ['sein'], 'gehen → sein; dugaan tentang masa lalu'],
        ['f', 'In einem Jahr wird sie ihr Studium abgeschlossen ___. (haben/sein)', ['haben'], 'abschließen → haben'],
        ['m', '„Sie wird den Termin vergessen haben.“ berarti …', ['Dia mungkin sudah lupa janjinya.', 'Dia akan lupa janjinya.', 'Dia harus melupakan janjinya.', 'Dia sedang melupakan janjinya.'], 'Futur II + konteks sekarang → dugaan tentang masa lalu'],
        ['m', 'Bis du zurückkommst, ___ ich aufgeräumt haben.', ['werde', 'wird', 'habe', 'bin'], 'ich → werde … aufgeräumt haben'],
        ['o', 'Bis Freitag werde ich die Arbeit beendet haben.', 'Sampai Jumat saya sudah akan menyelesaikan pekerjaan itu.', 'werden + Partizip II + haben']
      ]
    }
  ]
});
