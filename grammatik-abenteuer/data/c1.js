/* Level C1 – Bibliothek der Weisen (Perpustakaan Para Bijak) */
window.GRAMMATIK_LEVELS = window.GRAMMATIK_LEVELS || [];
window.GRAMMATIK_LEVELS.push({
  id: 'C1',
  title: 'Bibliothek der Weisen',
  subtitle: 'Perpustakaan Para Bijak',
  icon: '📜',
  color: '#c0392b',
  intro: 'Akhirnya kamu sampai di <b>Bibliothek der Weisen</b>, perpustakaan kuno tempat tersimpannya rahasia tertinggi bahasa Jerman. Sang <b>Grammatik-Lich</b> menguasai gaya bahasa akademik dan berita. Kalahkan dia untuk menjadi <b>Grammatik-Meister</b> sejati!',
  boss: ['Der Grammatik-Lich', '💀'],
  topics: [
    {
      id: 'c1-1',
      title: 'Konjunktiv I – indirekte Rede',
      sub: 'Kalimat tidak langsung dalam berita & teks ilmiah',
      monster: ['Nachrichten-Papagei', '📰'],
      theory: `
        <p><b>Konjunktiv I</b> dipakai untuk <b>melaporkan ucapan orang lain</b> secara netral — sangat sering di berita, koran, dan teks akademik.</p>
        <h3>Bentuk: Stamm + -e, -est, -e, -en, -et, -en</h3>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th></th><th>sein</th><th>haben</th><th>können</th><th>kommen</th><th>werden</th></tr>
          <tr><td>ich</td><td>sei</td><td>(hätte)</td><td>könne</td><td>(käme)</td><td>(würde)</td></tr>
          <tr><td>du</td><td>sei(e)st</td><td>habest</td><td>könnest</td><td>kommest</td><td>werdest</td></tr>
          <tr><td><b>er/sie/es</b></td><td><span class="de">sei</span></td><td><span class="de">habe</span></td><td><span class="de">könne</span></td><td><span class="de">komme</span></td><td><span class="de">werde</span></td></tr>
          <tr><td>wir</td><td>seien</td><td>(hätten)</td><td>(könnten)</td><td>(kämen)</td><td>(würden)</td></tr>
          <tr><td>sie/Sie</td><td>seien</td><td>(hätten)</td><td>(könnten)</td><td>(kämen)</td><td>(würden)</td></tr>
        </table></div>
        <div class="tip">💡 <b>Aturan pengganti</b>: Jika Konjunktiv I sama dengan Indikativ (mis. <i>sie haben</i>), gunakan <b>Konjunktiv II</b> (<i>sie hätten</i>) — itulah bentuk dalam kurung di tabel.</div>
        <h3>Waktu dalam indirekte Rede</h3>
        <ul>
          <li>Sekarang: <i>Er sagt, er <b>sei</b> müde.</i></li>
          <li>Lampau: <b>habe/sei + Partizip II</b> → <i>Sie sagt, sie <b>habe</b> nichts <b>gewusst</b>.</i></li>
          <li>Masa depan: <b>werde + Infinitiv</b> → <i>Er sagt, er <b>werde</b> morgen <b>kommen</b>.</i></li>
          <li>Pertanyaan: <i>Sie fragte, <b>ob</b> er Zeit <b>habe</b>.</i> Perintah: <i>Er sagte, ich <b>solle</b> warten.</i></li>
        </ul>`,
      examples: [
        ['Der Minister sagte, er sei optimistisch.', 'Menteri itu mengatakan bahwa ia optimis.'],
        ['Sie erklärte, sie habe keine Zeit.', 'Dia menjelaskan bahwa dia tidak punya waktu.'],
        ['Er behauptet, er habe das Geld nicht gestohlen.', 'Dia mengklaim tidak mencuri uang itu.'],
        ['Die Experten sagen, sie hätten keine Beweise.', 'Para ahli mengatakan mereka tidak punya bukti.'],
        ['Sie fragte, ob er morgen komme.', 'Dia bertanya apakah dia (lk) datang besok.']
      ],
      ex: [
        ['m', 'Der Sprecher sagte, die Lage ___ unter Kontrolle.', ['sei', 'ist', 'wäre', 'sein'], 'Konjunktiv I dari sein (er/sie/es) → sei'],
        ['m', 'Sie behauptet, sie ___ den Brief nie bekommen.', ['habe', 'hat', 'hatte', 'haben'], 'Lampau dalam Konjunktiv I: habe + Partizip II'],
        ['m', 'Die Forscher erklärten, sie ___ neue Daten.', ['hätten', 'haben', 'habe', 'hatten'], '"sie haben" = sama dengan Indikativ → pakai Konjunktiv II: hätten'],
        ['f', 'Er sagte, er ___ morgen nach Wien. (fahren, Konjunktiv I)', ['fahre'], 'fahren → er fahre'],
        ['f', 'Laut Polizei ___ der Täter ins Ausland geflohen. (sein, Konjunktiv I)', ['sei'], 'Lampau: sei + Partizip II (fliehen → sein)'],
        ['m', 'Ubah ke indirekte Rede: Er sagt: „Ich komme später.“', ['Er sagt, er komme später.', 'Er sagt, ich komme später.', 'Er sagt, er kommt später worden.', 'Er sagt, er käme später gewesen.'], 'Ganti pronomen (ich → er) + Konjunktiv I: komme'],
        ['o', 'Sie sagte, sie könne heute nicht kommen.', 'Dia bilang hari ini tidak bisa datang.', 'können → Konjunktiv I: könne']
      ]
    },
    {
      id: 'c1-2',
      title: 'Partizipialattribute',
      sub: 'der lachende Junge · das gestern gekaufte Brot · die zu lösende Aufgabe',
      monster: ['Partizip-Sphinx', '🦁'],
      theory: `
        <p>Dalam bahasa akademik, keterangan panjang sering diletakkan <b>di antara artikel dan kata benda</b>. Strukturnya:</p>
        <p style="text-align:center"><b>Artikel</b> + [keterangan] + <b>Partizip + akhiran kata sifat</b> + <b>Nomen</b></p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Jenis</th><th>Makna</th><th>Contoh</th><th>= Relativsatz</th></tr>
          <tr><td><b>Partizip I</b> (Infinitiv + d)</td><td>aktif, sedang berlangsung</td><td>der <b>seit Jahren in Berlin lebende</b> Künstler</td><td>der Künstler, der seit Jahren in Berlin lebt</td></tr>
          <tr><td><b>Partizip II</b></td><td>pasif / sudah selesai</td><td>das <b>gestern gekaufte</b> Brot</td><td>das Brot, das gestern gekauft wurde</td></tr>
          <tr><td><b>zu + Partizip I</b></td><td>harus / dapat di-…</td><td>die <b>noch zu lösenden</b> Probleme</td><td>die Probleme, die noch gelöst werden müssen</td></tr>
        </table></div>
        <div class="tip">💡 Cara membaca: temukan <b>artikel</b>, lompat ke <b>kata benda</b>-nya, lalu baca keterangan di tengah. Partizip mendapat akhiran kata sifat biasa (-e, -en, -er, -es).</div>`,
      examples: [
        ['Die lachenden Kinder spielen im Park.', 'Anak-anak yang tertawa bermain di taman.'],
        ['Das gestern gekaufte Brot ist schon hart.', 'Roti yang dibeli kemarin sudah keras.'],
        ['Der seit Jahren in Berlin lebende Künstler stellt aus.', 'Seniman yang sudah bertahun-tahun tinggal di Berlin berpameran.'],
        ['Die von der Regierung beschlossenen Maßnahmen gelten ab Montag.', 'Tindakan yang diputuskan pemerintah berlaku mulai Senin.'],
        ['Die noch zu erledigenden Aufgaben liegen auf dem Tisch.', 'Tugas-tugas yang masih harus diselesaikan ada di meja.']
      ],
      ex: [
        ['m', 'Der ___ Hund hat mich erschreckt. (anjing yang sedang menggonggong)', ['bellende', 'gebellte', 'bellte', 'bellender'], 'Aktif & sedang berlangsung → Partizip I + -e (setelah der)'],
        ['m', 'Das ___ Fenster muss repariert werden. (sudah pecah)', ['zerbrochene', 'zerbrechende', 'zerbrochen', 'zerbrechene'], 'Sudah selesai/pasif → Partizip II + -e'],
        ['m', '„die zu lösenden Probleme“ berarti …', ['die Probleme, die gelöst werden müssen', 'die Probleme, die gelöst wurden', 'die Probleme, die sich gelöst haben', 'die Probleme, die lösen'], 'zu + Partizip I = harus / dapat di-…'],
        ['m', '„der seit 2010 in Wien lebende Autor“ = …', ['der Autor, der seit 2010 in Wien lebt', 'der Autor, der seit 2010 in Wien gelebt wird', 'der Autor, den seit 2010 in Wien lebt', 'der Autor, der seit 2010 in Wien gelebt wurde'], 'Partizip I → aktif, Präsens: der … lebt'],
        ['f', 'Die von vielen Menschen ___ Ausstellung war ein Erfolg. (besuchen)', ['besuchte'], 'Pasif → Partizip II besucht + -e: die … besuchte Ausstellung'],
        ['f', 'Wir suchen eine gut Deutsch ___ Mitarbeiterin. (sprechen, Partizip I)', ['sprechende'], 'Aktif → Partizip I sprechend + -e: eine … sprechende Mitarbeiterin'],
        ['o', 'Das vor kurzem renovierte Museum ist wieder geöffnet.', 'Museum yang baru saja direnovasi sudah dibuka kembali.', 'Artikel + keterangan + Partizip II + Nomen']
      ]
    },
    {
      id: 'c1-3',
      title: 'Nominalisierung (Nominalstil)',
      sub: 'Mengubah anak kalimat menjadi frasa benda',
      monster: ['Nominal-Minotaurus', '🐂'],
      theory: `
        <p>Teks ilmiah dan resmi memakai <b>Nominalstil</b>: anak kalimat diubah menjadi frasa kata benda + preposisi. Hasilnya lebih ringkas dan formal.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Verbalstil (konjungsi)</th><th>Nominalstil (preposisi)</th></tr>
          <tr><td>weil es stark regnete</td><td><b>wegen / aufgrund</b> des starken Regens</td></tr>
          <tr><td>obwohl sie krank war</td><td><b>trotz</b> ihrer Krankheit</td></tr>
          <tr><td>nachdem er angekommen war</td><td><b>nach</b> seiner Ankunft</td></tr>
          <tr><td>bevor die Prüfung beginnt</td><td><b>vor</b> Beginn der Prüfung</td></tr>
          <tr><td>wenn es regnet</td><td><b>bei</b> Regen</td></tr>
          <tr><td>während wir arbeiten</td><td><b>während</b> der Arbeit</td></tr>
          <tr><td>indem man Sport treibt</td><td><b>durch</b> Sport</td></tr>
          <tr><td>um Kosten zu sparen</td><td><b>zur</b> Einsparung von Kosten</td></tr>
        </table></div>
        <h3>Aturan perubahan</h3>
        <ul>
          <li>Kata kerja → kata benda: entscheiden → die Entscheidung, ankommen → die Ankunft, beginnen → der Beginn</li>
          <li>Subjek → <b>Genitiv</b> atau kata ganti milik: <i>die Regierung entscheidet → die Entscheidung <b>der Regierung</b></i></li>
          <li>Objek → Genitiv atau <b>von</b>: <i>Kosten sparen → die Einsparung <b>von</b> Kosten</i></li>
          <li>Keterangan → kata sifat: <i>es regnet stark → der <b>starke</b> Regen</i></li>
        </ul>`,
      examples: [
        ['Wegen des starken Regens fiel das Spiel aus.', 'Karena hujan lebat, pertandingan dibatalkan.'],
        ['Nach seiner Ankunft rief er sofort an.', 'Setelah tiba, dia langsung menelepon.'],
        ['Trotz ihrer Krankheit ging sie zur Arbeit.', 'Meskipun sakit, dia tetap bekerja.'],
        ['Bei Regen findet das Fest im Saal statt.', 'Jika hujan, pesta diadakan di aula.'],
        ['Die Entscheidung der Regierung wurde kritisiert.', 'Keputusan pemerintah dikritik.']
      ],
      ex: [
        ['m', '„Nachdem wir angekommen waren, …“ → „___ unserer Ankunft …“', ['Nach', 'Vor', 'Bei', 'Seit'], 'nachdem → nach'],
        ['m', '„Obwohl er krank war, …“ → „___ seiner Krankheit …“', ['Trotz', 'Wegen', 'Bei', 'Durch'], 'obwohl → trotz'],
        ['m', '„Indem man regelmäßig Sport treibt, …“ → „___ regelmäßigen Sport …“', ['Durch', 'Trotz', 'Vor', 'Statt'], 'indem → durch + Akkusativ'],
        ['f', '„Wenn es regnet, …“ → „___ Regen …“', ['Bei'], 'wenn (syarat) → bei'],
        ['m', '„Die Regierung entscheidet.“ → „die ___ der Regierung“', ['Entscheidung', 'Entscheidheit', 'Entscheidnis', 'Entschiedung'], 'entscheiden → die Entscheidung; subjek → Genitiv'],
        ['f', '„Um Kosten zu sparen, …“ → „___ Einsparung von Kosten …“', ['Zur'], 'um … zu → zu + der = zur'],
        ['o', 'Wegen des hohen Verkehrsaufkommens kam es zu Verspätungen.', 'Karena volume lalu lintas yang tinggi, terjadi keterlambatan.', 'Nominalstil: wegen + Genitiv']
      ]
    },
    {
      id: 'c1-4',
      title: 'Passiversatzformen',
      sub: 'sein + zu · sich lassen · -bar / -lich',
      monster: ['Ersatz-Chimäre', '🦄'],
      theory: `
        <p>Selain <i>werden</i>, bahasa Jerman punya cara lain untuk mengungkapkan makna pasif, sering dengan tambahan makna <b>"dapat"</b> atau <b>"harus"</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Bentuk</th><th>Makna</th><th>Contoh</th><th>= Passiv</th></tr>
          <tr><td><b>sein + zu + Infinitiv</b></td><td>harus / dapat</td><td>Die Regeln <b>sind</b> zu beachten.</td><td>… müssen beachtet werden.</td></tr>
          <tr><td><b>sich lassen + Infinitiv</b></td><td>dapat</td><td>Das Problem <b>lässt sich</b> lösen.</td><td>… kann gelöst werden.</td></tr>
          <tr><td><b>Adjektiv -bar</b></td><td>dapat</td><td>Das Wasser ist trink<b>bar</b>.</td><td>… kann getrunken werden.</td></tr>
          <tr><td><b>Adjektiv -lich</b></td><td>dapat</td><td>Der Text ist verständ<b>lich</b>.</td><td>… kann verstanden werden.</td></tr>
          <tr><td><b>man</b></td><td>netral</td><td><b>Man</b> repariert das Auto.</td><td>Das Auto wird repariert.</td></tr>
        </table></div>
        <div class="tip">💡 Negatif dengan awalan <b>un-</b>: unlösbar (tidak dapat dipecahkan), unvermeidlich (tidak dapat dihindari), unbezahlbar (tak ternilai/terlalu mahal).</div>`,
      examples: [
        ['Das Problem lässt sich leicht lösen.', 'Masalah itu mudah dipecahkan.'],
        ['Die Formulare sind bis Montag abzugeben.', 'Formulir harus diserahkan sampai Senin.'],
        ['Dieses Wasser ist nicht trinkbar.', 'Air ini tidak bisa diminum.'],
        ['Seine Handschrift ist kaum lesbar.', 'Tulisan tangannya hampir tak terbaca.'],
        ['Diese Entwicklung war unvermeidlich.', 'Perkembangan ini tidak dapat dihindari.']
      ],
      ex: [
        ['m', '„Das Problem kann gelöst werden.“ = „Das Problem lässt sich ___.“', ['lösen', 'gelöst', 'zu lösen', 'lösbar'], 'sich lassen + Infinitiv'],
        ['m', '„Die Regeln müssen beachtet werden.“ = „Die Regeln sind ___.“', ['zu beachten', 'beachtet zu', 'beachtbar', 'beachtend'], 'sein + zu + Infinitiv = harus'],
        ['m', '„Der Text kann gelesen werden.“ = „Der Text ist ___.“', ['lesbar', 'lesend', 'gelesen', 'lesig'], '-bar = dapat di-…'],
        ['f', 'Dieser Fehler ist leicht zu ___. (vermeiden)', ['vermeiden'], 'sein + zu + Infinitiv'],
        ['f', 'Die Tür lässt sich nicht ___. (öffnen)', ['öffnen'], 'sich lassen + Infinitiv'],
        ['m', 'Arti "Die Kosten sind nicht bezahlbar." adalah …', ['Biayanya tidak terjangkau.', 'Biayanya sudah dibayar.', 'Biayanya harus dibayar.', 'Biayanya sedang dibayar.'], '-bar = dapat; nicht bezahlbar = tidak dapat dibayar/terjangkau'],
        ['o', 'Diese Aufgabe ist bis morgen zu erledigen.', 'Tugas ini harus diselesaikan sampai besok.', 'sein + zu + Infinitiv = harus']
      ]
    },
    {
      id: 'c1-5',
      title: 'Modalpartikeln',
      sub: 'doch, mal, ja, denn, eben, halt, wohl …',
      monster: ['Partikel-Phönix', '🔥'],
      theory: `
        <p><b>Modalpartikeln</b> adalah kata kecil yang <b>tidak mengubah isi</b> kalimat, tetapi memberi <b>nuansa emosi</b>. Penutur asli memakainya terus-menerus — kunci untuk terdengar alami!</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Partikel</th><th>Fungsi</th><th>Contoh</th></tr>
          <tr><td><b>doch</b></td><td>ajakan mendesak, kontras, mengingatkan</td><td>Komm <b>doch</b> mit! · Das weißt du <b>doch</b>!</td></tr>
          <tr><td><b>mal</b></td><td>melunakkan perintah/permintaan</td><td>Schau <b>mal</b>! · Kannst du <b>mal</b> helfen?</td></tr>
          <tr><td><b>ja</b></td><td>hal yang sudah diketahui bersama; kagum</td><td>Du weißt <b>ja</b>, … · Das ist <b>ja</b> toll!</td></tr>
          <tr><td><b>denn</b></td><td>pertanyaan ramah / penasaran</td><td>Was ist <b>denn</b> los?</td></tr>
          <tr><td><b>eben / halt</b></td><td>pasrah, tak bisa diubah</td><td>Das ist <b>eben</b> so.</td></tr>
          <tr><td><b>wohl</b></td><td>dugaan</td><td>Er ist <b>wohl</b> krank.</td></tr>
          <tr><td><b>eigentlich</b></td><td>pertanyaan sambil lalu / topik baru</td><td>Wie alt bist du <b>eigentlich</b>?</td></tr>
          <tr><td><b>bloß / nur</b></td><td>peringatan; harapan</td><td>Mach das <b>bloß</b> nicht! · Wenn ich <b>nur</b> Zeit hätte!</td></tr>
        </table></div>
        <div class="tip">💡 Kombinasi umum: <i>doch mal</i> (Komm doch mal vorbei!), <i>ja wohl</i>, <i>halt mal</i>.</div>`,
      examples: [
        ['Komm doch mal vorbei!', 'Mampirlah sesekali!'],
        ['Was ist denn los?', 'Ada apa sih?'],
        ['Das ist ja eine tolle Idee!', 'Wah, itu ide yang hebat!'],
        ['Das ist halt so.', 'Ya memang begitu.'],
        ['Mach das bloß nicht noch einmal!', 'Awas, jangan lakukan itu lagi!']
      ],
      ex: [
        ['m', 'Was ist ___ los? Du siehst traurig aus.', ['denn', 'mal', 'eben', 'halt'], 'Pertanyaan penuh perhatian → denn'],
        ['m', 'Kannst du mir ___ kurz helfen?', ['mal', 'denn', 'eben', 'wohl'], 'Melunakkan permintaan → mal'],
        ['m', 'Du kennst ___ meinen Bruder – er ist Arzt.', ['ja', 'denn', 'mal', 'bloß'], 'Info yang sudah diketahui bersama → ja'],
        ['m', 'Das ist ___ so, daran kann man nichts ändern.', ['eben', 'denn', 'mal', 'bloß'], 'Pasrah, tak bisa diubah → eben / halt'],
        ['f', 'Komm ___ mit! Es wird bestimmt lustig. (ajakan mendesak)', ['doch', 'doch mal'], 'Ajakan mendesak → doch'],
        ['m', 'Er ist heute nicht da. Er ist ___ krank. (dugaan)', ['wohl', 'denn', 'mal', 'bloß'], 'Dugaan → wohl'],
        ['o', 'Das ist ja eine tolle Überraschung!', 'Wah, ini kejutan yang luar biasa!', 'ja = ungkapan heran/kagum']
      ]
    },
    {
      id: 'c1-6',
      title: 'Subjektive Modalverben',
      sub: 'Dugaan & klaim: muss, dürfte, könnte, soll, will',
      monster: ['Orakel-Schlange', '🔮'],
      theory: `
        <p>Modalverben juga bisa menyatakan <b>tingkat keyakinan pembicara</b> atau <b>sumber informasi</b>.</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Modal</th><th>Makna</th><th>Contoh</th></tr>
          <tr><td><b>muss</b></td><td>hampir pasti (± 90%)</td><td>Er <b>muss</b> zu Hause sein, das Licht brennt.</td></tr>
          <tr><td><b>dürfte</b></td><td>kemungkinan besar (± 75%)</td><td>Sie <b>dürfte</b> etwa 40 sein.</td></tr>
          <tr><td><b>kann / könnte</b></td><td>mungkin (± 50%)</td><td>Es <b>könnte</b> morgen regnen.</td></tr>
          <tr><td><b>soll</b></td><td>kabarnya / kata orang</td><td>Der Chef <b>soll</b> sehr streng sein.</td></tr>
          <tr><td><b>will</b></td><td>klaim dari subjek sendiri (diragukan)</td><td>Er <b>will</b> den Täter gesehen haben.</td></tr>
        </table></div>
        <h3>Dugaan tentang masa lalu</h3>
        <p><b>Modalverb + Partizip II + haben/sein</b> (Infinitiv Perfekt):</p>
        <p><i>Er <b>muss</b> den Zug <b>verpasst haben</b>.</i> (Dia pasti ketinggalan kereta.)<br><i>Sie <b>soll</b> nach Kanada <b>ausgewandert sein</b>.</i> (Kabarnya dia beremigrasi ke Kanada.)</p>`,
      examples: [
        ['Er muss krank sein, er sieht blass aus.', 'Dia pasti sakit, wajahnya pucat.'],
        ['Das dürfte kein Problem sein.', 'Itu kemungkinan besar bukan masalah.'],
        ['Der Film soll sehr gut sein.', 'Katanya filmnya sangat bagus.'],
        ['Er will das alles allein gemacht haben.', 'Dia mengaku melakukan semuanya sendiri.'],
        ['Der Zug muss schon abgefahren sein.', 'Keretanya pasti sudah berangkat.']
      ],
      ex: [
        ['m', 'Bei ihm brennt Licht. Er ___ zu Hause sein. (hampir pasti)', ['muss', 'soll', 'will', 'darf'], 'müssen = hampir pasti'],
        ['m', 'Man sagt, der neue Chef ___ sehr streng sein. (kabarnya)', ['soll', 'will', 'muss', 'darf'], 'sollen = kata orang / kabarnya'],
        ['m', 'Er ___ den Unfall gesehen haben, aber niemand glaubt ihm.', ['will', 'soll', 'muss', 'dürfte'], 'wollen = klaim dari subjek sendiri'],
        ['m', 'Es ___ morgen regnen – sicher bin ich mir aber nicht.', ['könnte', 'muss', 'will', 'darf'], 'könnte = mungkin (tidak yakin)'],
        ['f', 'Sie ist nicht gekommen. Sie muss den Termin vergessen ___.', ['haben'], 'Dugaan masa lalu: Modal + Partizip II + haben'],
        ['m', '„Er dürfte etwa 40 sein.“ berarti …', ['Dia kemungkinan besar sekitar 40 tahun.', 'Dia boleh berumur 40 tahun.', 'Dia harus berumur 40 tahun.', 'Dia mengaku berumur 40 tahun.'], 'dürfte (Konjunktiv II) = dugaan kuat'],
        ['o', 'Der Zug muss schon abgefahren sein.', 'Keretanya pasti sudah berangkat.', 'Dugaan masa lalu: muss + Partizip II + sein']
      ]
    },
    {
      id: 'c1-7',
      title: 'Konzessive & adversative Konnektoren',
      sub: 'obwohl, trotzdem, wohingegen, zumal, es sei denn …',
      monster: ['Widerspruchs-Wyvern', '🌪️'],
      theory: `
        <p>Konektor tingkat C1 untuk menyatakan <b>pertentangan, konsesi, dan pengecualian</b>. Perhatikan <b>posisi kata kerjanya</b>!</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Jenis</th><th>Konektor</th><th>Posisi kata kerja</th><th>Contoh</th></tr>
          <tr><td>Anak kalimat</td><td>obwohl, obgleich, wenngleich (meskipun)</td><td>di akhir</td><td>Obwohl es regnete, gingen wir raus.</td></tr>
          <tr><td>Anak kalimat</td><td>wohingegen, während (sedangkan)</td><td>di akhir</td><td>Er liebt Sport, wohingegen ich lieber lese.</td></tr>
          <tr><td>Anak kalimat</td><td>zumal (apalagi karena)</td><td>di akhir</td><td>Ich bleibe hier, zumal es spät ist.</td></tr>
          <tr><td>Adverb</td><td>trotzdem, dennoch (namun tetap), allerdings</td><td>posisi 2 (inversi)</td><td>Es regnete. Trotzdem gingen wir raus.</td></tr>
          <tr><td>Kalimat utama</td><td>es sei denn (kecuali jika)</td><td>susunan normal</td><td>Wir fahren, es sei denn, es regnet.</td></tr>
        </table></div>
        <div class="tip">💡 <i>obwohl</i> memulai anak kalimat; <i>trotzdem</i> adalah adverb yang menempati posisi 1 → kata kerja langsung setelahnya.</div>`,
      examples: [
        ['Obwohl es spät war, arbeitete sie weiter.', 'Meskipun sudah larut, dia terus bekerja.'],
        ['Er war müde, trotzdem fuhr er weiter.', 'Dia lelah, namun tetap melanjutkan perjalanan.'],
        ['Mein Bruder liebt Sport, wohingegen ich lieber lese.', 'Kakakku suka olahraga, sedangkan aku lebih suka membaca.'],
        ['Ich nehme das Angebot an, zumal es sehr günstig ist.', 'Saya menerima tawaran itu, apalagi harganya sangat murah.'],
        ['Wir treffen uns um acht, es sei denn, du hast keine Zeit.', 'Kita bertemu jam delapan, kecuali kamu tidak ada waktu.']
      ],
      ex: [
        ['m', 'Es regnete stark. ___ gingen wir wandern.', ['Trotzdem', 'Obwohl', 'Wohingegen', 'Zumal'], 'Adverb di posisi 1 → kata kerja langsung setelahnya'],
        ['m', '___ er wenig Zeit hatte, half er mir.', ['Obwohl', 'Trotzdem', 'Dennoch', 'Allerdings'], 'Anak kalimat konsesif (kata kerja di akhir) → obwohl'],
        ['m', 'Wir fahren morgen los, ___, es regnet.', ['es sei denn', 'obwohl', 'zumal', 'dennoch'], 'es sei denn = kecuali jika'],
        ['m', 'Sie bleibt heute zu Hause, ___ sie ohnehin erkältet ist.', ['zumal', 'obwohl', 'wohingegen', 'dennoch'], 'zumal = apalagi karena (alasan tambahan)'],
        ['m', 'Mein Bruder liebt Sport, ___ ich lieber lese.', ['wohingegen', 'trotzdem', 'zumal', 'dennoch'], 'Kontras dua hal → wohingegen'],
        ['f', 'Er war sehr müde, ___ arbeitete er weiter. (namun tetap)', ['trotzdem', 'dennoch'], 'Adverb konsesif → trotzdem / dennoch + inversi'],
        ['o', 'Obwohl es spät war, arbeitete sie weiter.', 'Meskipun sudah larut, dia terus bekerja.', 'obwohl → anak kalimat; induk kalimat dimulai kata kerja']
      ]
    }
  ]
});
