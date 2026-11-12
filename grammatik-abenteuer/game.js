/* =========================================================
   Grammatik-Abenteuer – mesin game
   Data materi ada di folder data/ (a1.js … c1.js).
   Format soal:
     ['m', pertanyaan, [jawabanBenar, salah1, salah2, ...], penjelasan]  → pilihan ganda
     ['f', pertanyaan dengan ___, [jawaban yang diterima...], penjelasan] → isi titik-titik
     ['o', kalimat benar (dipisah spasi), terjemahan, penjelasan]         → susun kalimat
   ========================================================= */
(function () {
  'use strict';

  const LEVELS = window.GRAMMATIK_LEVELS || [];
  const SAVE_KEY = 'grammatikAbenteuer_v1';
  const HEROES = ['🧙‍♀️', '🧝‍♂️', '🦸‍♀️', '🧑‍🚀', '🥷', '🧚‍♀️', '🤴', '👸'];
  const SHOP = {
    potion: { icon: '🧪', name: 'Heiltrank', desc: 'Memulihkan 1 ❤️ saat bertarung.', price: 20 },
    hint: { icon: '💡', name: 'Hinweis', desc: 'Bantuan: hapus 2 pilihan salah, tampilkan huruf awal, atau pasang kata pertama.', price: 15 }
  };
  const BOSS_QUESTIONS = 12;
  const REVIEW_QUESTIONS = 10;

  const app = document.getElementById('app');
  const toastEl = document.getElementById('toast');

  // Indeks cepat: id topik → {lv, li, t, ti}
  const TOPICS = {};
  LEVELS.forEach((lv, li) => lv.topics.forEach((t, ti) => { TOPICS[t.id] = { lv, li, t, ti }; }));

  let S = loadSave();          // data pemain (disimpan di localStorage)
  let battle = null;           // pertarungan yang sedang berlangsung
  let chosenHero = HEROES[0];  // pilihan avatar di layar awal

  /* ---------- Utilitas ---------- */
  function esc(s) {
    return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  }
  function shuffle(arr) {
    const a = arr.slice();
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }
  function norm(s) {
    return String(s).trim().toLowerCase()
      .replace(/ß/g, 'ss').replace(/[„“"'.!?]/g, '').replace(/\s+/g, ' ');
  }
  function starsHtml(n, max = 3) {
    let h = '<span class="stars" aria-label="' + n + ' dari ' + max + ' bintang">';
    for (let i = 0; i < max; i++) h += i < n ? '★' : '<span class="off">★</span>';
    return h + '</span>';
  }
  function blankify(text) {
    return esc(text).replace(/___/g, '<span class="blank">?</span>');
  }
  function toast(msg) {
    toastEl.textContent = msg;
    toastEl.classList.add('show');
    clearTimeout(toast._t);
    toast._t = setTimeout(() => toastEl.classList.remove('show'), 2200);
  }
  function confetti() {
    const bits = ['🎉', '⭐', '✨', '🪙', '🎊'];
    for (let i = 0; i < 24; i++) {
      const el = document.createElement('div');
      el.className = 'confetti';
      el.textContent = bits[i % bits.length];
      el.style.left = Math.random() * 100 + 'vw';
      el.style.animationDuration = (1.8 + Math.random() * 1.8) + 's';
      el.style.animationDelay = (Math.random() * .6) + 's';
      document.body.appendChild(el);
      setTimeout(() => el.remove(), 4500);
    }
  }

  /* ---------- Simpan / muat ---------- */
  function newSave(name, hero) {
    return { name, hero, xp: 0, coins: 30, potions: 1, hints: 2, stars: {}, bosses: {}, mistakes: [], blitz: {}, unlockAll: false, sound: true };
  }
  function loadSave() {
    try {
      const raw = localStorage.getItem(SAVE_KEY);
      if (!raw) return null;
      const data = JSON.parse(raw);
      return Object.assign(newSave('Held', HEROES[0]), data);
    } catch (e) { return null; }
  }
  function save() {
    try { localStorage.setItem(SAVE_KEY, JSON.stringify(S)); } catch (e) { /* mode privat: abaikan */ }
  }
  function playerLevel() { return Math.floor(S.xp / 100) + 1; }

  /* ---------- Suara (efek & pelafalan) ---------- */
  let audioCtx = null;
  function sfx(type) {
    if (!S || !S.sound) return;
    try {
      audioCtx = audioCtx || new (window.AudioContext || window.webkitAudioContext)();
      const seq = {
        ok: [[660, .08], [880, .14]],
        bad: [[220, .15], [150, .22]],
        win: [[523, .1], [659, .1], [784, .1], [1047, .3]],
        lose: [[392, .18], [330, .18], [262, .35]],
        coin: [[988, .06], [1319, .12]]
      }[type];
      let t = audioCtx.currentTime;
      seq.forEach(([f, d]) => {
        const o = audioCtx.createOscillator(), g = audioCtx.createGain();
        o.type = 'triangle'; o.frequency.value = f;
        g.gain.setValueAtTime(.15, t);
        g.gain.exponentialRampToValueAtTime(.001, t + d);
        o.connect(g).connect(audioCtx.destination);
        o.start(t); o.stop(t + d); t += d;
      });
    } catch (e) { /* tanpa suara */ }
  }

  let deVoice = null;
  function pickVoice() {
    const vs = window.speechSynthesis.getVoices();
    deVoice = vs.find(v => /^de([-_]|$)/i.test(v.lang)) || null;
  }
  if ('speechSynthesis' in window) {
    pickVoice();
    window.speechSynthesis.onvoiceschanged = pickVoice;
  }
  function speak(text) {
    if (!('speechSynthesis' in window)) { toast('Browser ini belum mendukung suara 🔇'); return; }
    window.speechSynthesis.cancel();
    const u = new SpeechSynthesisUtterance(text);
    u.lang = 'de-DE';
    if (deVoice) u.voice = deVoice;
    u.rate = .9;
    window.speechSynthesis.speak(u);
  }

  /* ---------- Aturan buka kunci ---------- */
  function regionUnlocked(li) {
    return li === 0 || S.unlockAll || !!S.bosses[LEVELS[li - 1].id];
  }
  function topicUnlocked(li, ti) {
    if (!regionUnlocked(li)) return false;
    if (ti === 0 || S.unlockAll) return true;
    return (S.stars[LEVELS[li].topics[ti - 1].id] || 0) > 0;
  }
  function bossUnlocked(li) {
    if (!regionUnlocked(li)) return false;
    return S.unlockAll || LEVELS[li].topics.every(t => (S.stars[t.id] || 0) > 0);
  }
  function regionStars(lv) {
    return lv.topics.reduce((sum, t) => sum + (S.stars[t.id] || 0), 0);
  }

  /* ---------- Render helper ---------- */
  let lastKey = null;
  // key: identitas layar. Animasi & scroll ke atas hanya saat layar berganti,
  // supaya tidak berkedip ketika layar yang sama di-render ulang.
  function render(html, focusSel, key) {
    const changed = !key || key !== lastKey;
    lastKey = key || null;
    app.innerHTML = '<div class="' + (changed ? 'screen' : '') + '">' + html + '</div>';
    if (changed) window.scrollTo(0, 0);
    const f = focusSel && app.querySelector(focusSel);
    if (f) f.focus();
  }
  function hud() {
    const lvl = playerLevel();
    return `<div class="hud">
      <div class="avatar" aria-hidden="true">${S.hero}</div>
      <div class="info">
        <div class="name">${esc(S.name)} · Lv ${lvl}</div>
        <div class="xpbar" title="XP ${S.xp}"><span style="width:${S.xp % 100}%"></span></div>
      </div>
      <div class="stats"><span title="Koin">🪙 ${S.coins}</span><span title="Ramuan">🧪 ${S.potions}</span><span title="Petunjuk">💡 ${S.hints}</span></div>
    </div>`;
  }
  function nav(active) {
    const items = [
      ['map', '🗺️', 'Peta'],
      ['blitzMenu', '⚡', 'Blitz'],
      ['review', '📕', 'Fehlerheft' + (S.mistakes.length ? ' (' + S.mistakes.length + ')' : '')],
      ['shop', '🛒', 'Toko'],
      ['settings', '⚙️', 'Pengaturan']
    ];
    return '<nav class="bottom-nav" aria-label="Menu utama">' + items.map(([act, ic, label]) =>
      `<button data-act="${act === 'review' ? 'reviewScreen' : act}" ${active === act ? 'aria-current="page"' : ''}><span aria-hidden="true">${ic}</span>${label}</button>`
    ).join('') + '</nav>';
  }

  /* ---------- Layar: Awal ---------- */
  function screenStart() {
    const heroBtns = HEROES.map(h =>
      `<button class="hero-opt" data-act="pickHero" data-arg="${h}" aria-pressed="${h === chosenHero}" aria-label="Pilih avatar ${h}">${h}</button>`
    ).join('');
    render(`
      <div class="title-wrap">
        <div class="logo" aria-hidden="true">🏰</div>
        <h1>Grammatik-Abenteuer</h1>
        <p>Jelajahi 5 negeri, kalahkan monster grammatik, dan kuasai bahasa Jerman dari <b>A1</b> sampai <b>C1</b>!</p>
      </div>
      ${S ? `<div class="card center">
        <p>Selamat datang kembali, <b>${esc(S.name)}</b> ${S.hero}!</p>
        <button class="btn block" data-act="map">▶️ Lanjutkan Petualangan</button>
      </div>` : ''}
      <div class="card">
        <h2>${S ? 'Mulai petualangan baru' : 'Buat pahlawanmu'}</h2>
        <label class="field" for="heroName">Nama pahlawan</label>
        <input type="text" id="heroName" maxlength="20" placeholder="mis. Rina" autocomplete="off">
        <label class="field">Pilih avatar</label>
        <div class="hero-grid">${heroBtns}</div>
        <p></p>
        <button class="btn block blue" data-act="newGame">⚔️ Los geht's! (Ayo mulai!)</button>
        ${S ? '<p class="muted center" style="font-size:.85rem">Perhatian: memulai baru akan menghapus progres lama.</p>' : ''}
      </div>
      <div class="card">
        <h3>Cara bermain</h3>
        <ul>
          <li>📖 Baca <b>materi & contoh</b> tiap topik (tekan 🔊 untuk mendengar pelafalan).</li>
          <li>⚔️ Lawan monster dengan menjawab <b>latihan</b>: pilihan ganda, isi titik-titik, dan susun kalimat.</li>
          <li>❤️ Jawaban salah mengurangi nyawa — soalnya akan muncul lagi supaya kamu benar-benar paham.</li>
          <li>👑 Selesaikan semua topik lalu kalahkan <b>Boss</b> untuk membuka negeri berikutnya.</li>
          <li>📕 Soal yang salah masuk <b>Fehlerheft</b> (buku kesalahan) untuk diulang.</li>
        </ul>
      </div>
    `, S ? null : '#heroName');
  }

  /* ---------- Layar: Peta ---------- */
  function screenMap() {
    const regions = LEVELS.map((lv, li) => {
      const open = regionUnlocked(li);
      const done = lv.topics.filter(t => S.stars[t.id]).length;
      const beaten = S.bosses[lv.id];
      return `<button class="region" style="--rc:${lv.color}" data-act="region" data-arg="${li}" ${open ? '' : 'disabled'}>
        <div class="r-icon" aria-hidden="true">${lv.icon}</div>
        <div class="r-body">
          <span class="r-level">${lv.id}</span>
          <h3>${esc(lv.title)}</h3>
          <div class="r-prog">${esc(lv.subtitle)} · ${done}/${lv.topics.length} topik · ${regionStars(lv)}/${lv.topics.length * 3} ★</div>
        </div>
        <div class="lock" aria-hidden="true">${open ? (beaten ? '🏅' : '➡️') : '🔒'}</div>
        ${open ? '' : '<span class="sr-only" style="position:absolute;left:-9999px">Terkunci</span>'}
      </button>`;
    }).join('');
    const badges = LEVELS.map(lv =>
      `<span class="badge ${S.bosses[lv.id] ? '' : 'off'}">${S.bosses[lv.id] ? '🏅' : '⬜'} ${lv.id}</span>`
    ).join('');
    render(`
      ${hud()}
      <h2 class="light">🗺️ Peta Petualangan</h2>
      <div class="badges" aria-label="Lencana">${badges}</div>
      ${Object.keys(S.bosses).length ? '<button class="btn block" style="margin-bottom:16px" data-act="cert">📜 Lihat Sertifikat (Urkunde)</button>' : ''}
      <div class="map-path">${regions}</div>
      ${nav('map')}
    `);
  }

  /* ---------- Layar: Region ---------- */
  function screenRegion(li) {
    const lv = LEVELS[li];
    const nodes = lv.topics.map((t, ti) => {
      const open = topicUnlocked(li, ti);
      const st = S.stars[t.id] || 0;
      return `<button class="topic-node ${st ? 'done' : ''}" data-act="lesson" data-arg="${t.id}" ${open ? '' : 'disabled'}>
        <div class="num" aria-hidden="true">${open ? t.monster[1] : '🔒'}</div>
        <div class="t-body">
          <div class="t-title">${ti + 1}. ${esc(t.title)}</div>
          <div class="t-sub">${esc(t.sub)}</div>
        </div>
        <div class="t-stars">${starsHtml(st)}</div>
      </button>`;
    }).join('');
    const bOpen = bossUnlocked(li);
    render(`
      ${hud()}
      <div class="topbar">
        <button class="icon-btn" data-act="map" aria-label="Kembali ke peta">⬅</button>
        <h2>${lv.icon} ${lv.id} · ${esc(lv.title)}</h2>
      </div>
      <div class="card story" style="--rc:${lv.color}">
        <div class="big" aria-hidden="true">📜</div>
        <p style="margin:0">${lv.intro}</p>
      </div>
      ${nodes}
      <button class="topic-node boss" data-act="boss" data-arg="${li}" ${bOpen ? '' : 'disabled'}>
        <div class="num" aria-hidden="true">${bOpen ? lv.boss[1] : '🔒'}</div>
        <div class="t-body">
          <div class="t-title">👑 BOSS: ${esc(lv.boss[0])}</div>
          <div class="t-sub">${bOpen ? BOSS_QUESTIONS + ' soal campuran dari semua topik ' + lv.id + ' · 5 ❤️' : 'Selesaikan semua topik untuk membuka Boss'}</div>
        </div>
        <div class="t-stars">${S.bosses[lv.id] ? '🏅' : ''}</div>
      </button>
      ${nav('map')}
    `);
  }

  /* ---------- Layar: Materi ---------- */
  function screenLesson(tid) {
    const { lv, li, t } = TOPICS[tid];
    const examples = t.examples.map(([de, id]) => `<li>
        <button class="say" data-act="say" data-say="${esc(de)}" aria-label="Dengarkan: ${esc(de)}">🔊</button>
        <div><div class="ex-de">${esc(de)}</div><div class="ex-id">${esc(id)}</div></div>
      </li>`).join('');
    render(`
      <div class="topbar">
        <button class="icon-btn" data-act="region" data-arg="${li}" aria-label="Kembali">⬅</button>
        <h2>${lv.id} · ${esc(t.title)}</h2>
      </div>
      <div class="card lesson">
        <p class="muted" style="margin-top:0">📖 ${esc(t.sub)}</p>
        ${t.theory}
      </div>
      <div class="card">
        <h3>🗣️ Contoh kalimat</h3>
        <ul class="examples">${examples}</ul>
      </div>
      <div class="card center">
        <div style="font-size:3rem" aria-hidden="true">${t.monster[1]}</div>
        <p><b>${esc(t.monster[0])}</b> menghadang jalanmu! Jawab ${t.ex.length} soal untuk mengalahkannya.</p>
        <button class="btn block" data-act="fight" data-arg="${t.id}">⚔️ Mulai Latihan</button>
      </div>
    `, '.icon-btn');
  }

  /* ---------- Pertarungan ---------- */
  function startBattle(kind, ref) {
    let items = [], monster, hearts, title;
    if (kind === 'topic') {
      const { t } = TOPICS[ref];
      items = t.ex.map((q, i) => ({ q, tid: t.id, i }));
      monster = { name: t.monster[0], emoji: t.monster[1], boss: false };
      hearts = 3; title = t.title;
    } else if (kind === 'boss') {
      const lv = LEVELS[ref];
      lv.topics.forEach(t => t.ex.forEach((q, i) => items.push({ q, tid: t.id, i })));
      items = shuffle(items).slice(0, BOSS_QUESTIONS);
      monster = { name: lv.boss[0], emoji: lv.boss[1], boss: true };
      hearts = 5; title = 'Boss ' + lv.id;
    } else { // review
      items = shuffle(S.mistakes).slice(0, REVIEW_QUESTIONS).map(key => {
        const [tid, i] = key.split(':');
        const info = TOPICS[tid];
        return info && info.t.ex[+i] ? { q: info.t.ex[+i], tid, i: +i } : null;
      }).filter(Boolean);
      if (!items.length) { toast('Fehlerheft kosong. Hebat! 🎉'); return screenReview(); }
      monster = { name: 'Fehlergeist', emoji: '👻', boss: false };
      hearts = 5; title = 'Fehlerheft';
    }
    battle = {
      kind, ref, title, monster,
      queue: kind === 'topic' ? shuffle(items) : items,
      total: items.length, done: 0, correct: 0, mistakes: 0,
      hearts, maxHearts: hearts, cur: null, xpBefore: S.xp
    };
    nextQuestion();
  }

  function prepare(item) {
    const type = item.q[0];
    const cur = { item, type, answered: false, result: null };
    if (type === 'm') {
      cur.opts = shuffle(item.q[2].map((text, i) => ({ text, ok: i === 0, removed: false })));
    } else if (type === 'o') {
      cur.words = item.q[1].split(' ');
      let pool;
      let tries = 0;
      do { pool = shuffle(cur.words.map((w, i) => ({ w, i }))); tries++; }
      while (tries < 8 && cur.words.length > 1 && pool.map(p => p.w).join(' ') === item.q[1]);
      cur.pool = pool;
      cur.picked = [];
    } else {
      cur.hintText = '';
    }
    return cur;
  }

  function nextQuestion() {
    if (battle.done >= battle.total) return finishBattle(true);
    if (battle.hearts <= 0) return finishBattle(false);
    battle.cur = prepare(battle.queue.shift());
    battle.qNo = (battle.qNo || 0) + 1;
    screenBattle();
  }

  function correctAnswerText(cur) {
    const q = cur.item.q;
    if (cur.type === 'm') return q[2][0];
    if (cur.type === 'f') return q[2][0];
    return q[1];
  }
  function speakableSentence(cur) {
    const q = cur.item.q;
    if (cur.type === 'o') return q[1];
    const text = q[1];
    if (!text.includes('___') || /[„“→:]/.test(text)) return null;
    return text.replace(/\s*\([^)]*\)/g, '').replace('___', q[2][0]).trim();
  }

  function screenBattle() {
    const b = battle, cur = b.cur, q = cur.item.q;
    const hearts = '❤️'.repeat(Math.max(b.hearts, 0)) + '🖤'.repeat(b.maxHearts - Math.max(b.hearts, 0));
    const hpPct = Math.round(100 * (b.total - b.done) / b.total);
    const typeLabel = { m: '🔘 Pilihan ganda', f: '✏️ Isi titik-titik', o: '🧩 Susun kalimat' }[cur.type];
    let body = '';

    if (cur.type === 'm') {
      body = `<div class="q-text">${blankify(q[1])}</div>
        <div class="options">${cur.opts.map((o, i) => {
          let cls = 'opt';
          if (o.removed) cls += ' removed';
          if (cur.answered && o.ok) cls += ' correct';
          if (cur.answered && cur.result.given === i && !o.ok) cls += ' wrong';
          return `<button class="${cls}" data-act="answerMC" data-arg="${i}" ${cur.answered || o.removed ? 'disabled' : ''}><span class="key">${i + 1}</span>${esc(o.text)}</button>`;
        }).join('')}</div>`;
    } else if (cur.type === 'f') {
      body = `<div class="q-text">${blankify(q[1])}</div>
        <div class="fill-row">
          <input type="text" id="fillInput" autocomplete="off" autocapitalize="off" spellcheck="false"
            placeholder="${esc(cur.hintText ? cur.hintText + '…' : 'Ketik jawabanmu')}" aria-label="Jawaban"
            ${cur.answered ? 'disabled value="' + esc(cur.result.given) + '"' : ''}>
          <button class="btn blue" data-act="checkFill" ${cur.answered ? 'disabled' : ''}>Cek ✔</button>
        </div>`;
    } else {
      const answerChips = cur.picked.map((pi, pos) =>
        `<button class="chip" data-act="unpick" data-arg="${pos}" ${cur.answered ? 'disabled' : ''}>${esc(cur.pool[pi].w)}</button>`).join('');
      const poolChips = cur.pool.map((p, pi) =>
        `<button class="chip ${cur.picked.includes(pi) ? 'used' : ''}" data-act="pick" data-arg="${pi}" ${cur.answered || cur.picked.includes(pi) ? 'disabled' : ''}>${esc(p.w)}</button>`).join('');
      body = `<div class="q-text">Susun kalimat yang benar:</div>
        <div class="q-hint">🇮🇩 “${esc(q[2])}”</div>
        <div class="order-answer" aria-label="Kalimatmu">${answerChips || '<span class="muted">Ketuk kata di bawah…</span>'}</div>
        <div class="order-pool">${poolChips}</div>
        <div class="btn-row">
          <button class="btn secondary" data-act="resetOrder" ${cur.answered ? 'disabled' : ''}>↺ Ulang</button>
          <button class="btn blue" data-act="checkOrder" ${cur.answered || cur.picked.length !== cur.words.length ? 'disabled' : ''}>Cek ✔</button>
        </div>`;
    }

    let fb = '';
    if (cur.answered) {
      const ok = cur.result.ok;
      const sentence = speakableSentence(cur);
      fb = `<div class="feedback ${ok ? 'ok' : 'bad'}" role="alert">
        <div class="fb-title">${ok ? '✅ Richtig! Serangan berhasil!' : '❌ Falsch! Kamu terkena serangan.'}</div>
        ${ok ? '' : `<div class="fb-answer">Jawaban benar: ${esc(correctAnswerText(cur))}</div>`}
        <div class="fb-expl">💬 ${esc(q[3] || '')}</div>
        <div class="btn-row" style="margin-top:10px">
          ${sentence ? `<button class="btn secondary small" data-act="say" data-say="${esc(sentence)}">🔊 Dengarkan</button>` : ''}
          <button class="btn small" id="nextBtn" data-act="next">Weiter ➜</button>
        </div>
      </div>`;
    }

    render(`
      <div class="topbar">
        <button class="icon-btn" data-act="flee" aria-label="Kabur dari pertarungan">🏳️ Kabur</button>
        <h2>${esc(b.title)}</h2>
      </div>
      <div class="arena">
        <div class="fighter hero" id="heroSprite">
          <div class="hearts" aria-label="Nyawa ${b.hearts} dari ${b.maxHearts}">${hearts}</div>
          <div class="sprite">${S.hero}</div>
          <div class="fname">${esc(S.name)}</div>
        </div>
        <div class="vs">VS</div>
        <div class="fighter monster ${b.monster.boss ? 'boss' : ''}" id="monsterSprite" style="position:relative">
          <div class="hpbar" aria-label="HP monster ${hpPct}%"><span style="width:${hpPct}%"></span></div>
          <div class="sprite">${b.monster.emoji}</div>
          <div class="fname">${esc(b.monster.name)}</div>
        </div>
      </div>
      <div class="card">
        <span class="progress-txt">${b.done}/${b.total}</span>
        <div class="q-type">${typeLabel}</div>
        ${body}
        ${fb}
      </div>
      <div class="items">
        <button class="item-btn" data-act="usePotion" ${cur.answered || S.potions < 1 || b.hearts >= b.maxHearts ? 'disabled' : ''}>🧪 Ramuan (${S.potions})</button>
        <button class="item-btn" data-act="useHint" ${cur.answered || S.hints < 1 ? 'disabled' : ''}>💡 Petunjuk (${S.hints})</button>
      </div>
    `, cur.answered ? '#nextBtn' : (cur.type === 'f' ? '#fillInput' : null), 'battle:' + b.qNo);
  }

  function animate(id, cls) {
    const el = document.querySelector('#' + id + ' .sprite');
    if (!el) return;
    el.classList.remove(cls); void el.offsetWidth; el.classList.add(cls);
  }
  function floatText(id, text) {
    const host = document.getElementById(id);
    if (!host) return;
    const d = document.createElement('div');
    d.className = 'dmg'; d.textContent = text;
    d.style.left = '40%'; d.style.top = '20%';
    host.appendChild(d);
    setTimeout(() => d.remove(), 900);
  }

  function addMistake(key) { if (!S.mistakes.includes(key)) S.mistakes.push(key); }
  function removeMistake(key) { S.mistakes = S.mistakes.filter(k => k !== key); }

  function submit(ok, given) {
    const b = battle, cur = b.cur;
    if (cur.answered) return;
    cur.answered = true;
    cur.result = { ok, given };
    const key = cur.item.tid + ':' + cur.item.i;
    if (ok) {
      b.done++; b.correct++;
      if (b.kind === 'review') removeMistake(key);
      sfx('ok');
    } else {
      b.hearts--; b.mistakes++;
      b.queue.push(cur.item);           // soal yang salah akan muncul lagi
      addMistake(key);
      sfx('bad');
    }
    save();
    screenBattle();
    if (ok) {
      animate('heroSprite', 'attack');
      setTimeout(() => { animate('monsterSprite', 'hit'); floatText('monsterSprite', '-1 HP'); }, 180);
    } else {
      animate('heroSprite', 'hurt');
      floatText('heroSprite', '-❤️');
    }
  }

  function finishBattle(win) {
    const b = battle;
    const lvlBefore = Math.floor(b.xpBefore / 100) + 1;
    let stars = 0, xp = 0, coins = 0, extra = '';
    if (win) {
      stars = b.mistakes === 0 ? 3 : b.mistakes <= 2 ? 2 : 1;
      xp = b.correct * 5 + stars * 10;
      coins = stars * 5;
      if (b.kind === 'topic') {
        S.stars[b.ref] = Math.max(S.stars[b.ref] || 0, stars);
      } else if (b.kind === 'boss') {
        const lv = LEVELS[b.ref];
        const first = !S.bosses[lv.id];
        S.bosses[lv.id] = true;
        xp += 50; coins += 30;
        if (first) { S.potions += 1; S.hints += 1; extra = '🎁 Hadiah Boss: +1 🧪 +1 💡'; }
        if (b.ref === LEVELS.length - 1) extra += '<br>🏆 <b>Herzlichen Glückwunsch!</b> Kamu adalah <b>Grammatik-Meister C1</b>!';
        else if (first) extra += `<br>🔓 Negeri baru terbuka: <b>${esc(LEVELS[b.ref + 1].title)}</b> (${LEVELS[b.ref + 1].id})`;
      } else {
        coins = 10;
      }
      sfx('win'); confetti();
    } else {
      xp = b.correct * 2;
      sfx('lose');
    }
    S.xp += xp; S.coins += coins;
    save();
    const leveledUp = playerLevel() > lvlBefore;
    if (leveledUp) setTimeout(() => toast('⬆️ Level up! Sekarang Level ' + playerLevel()), 600);

    let nextBtn = '';
    if (win && b.kind === 'topic') {
      const { li, ti, lv } = TOPICS[b.ref];
      if (ti + 1 < lv.topics.length) nextBtn = `<button class="btn" data-act="lesson" data-arg="${lv.topics[ti + 1].id}">Topik berikutnya ➜</button>`;
      else nextBtn = `<button class="btn" data-act="region" data-arg="${li}">👑 Menuju Boss</button>`;
    } else if (win && b.kind === 'boss' && b.ref + 1 < LEVELS.length) {
      nextBtn = `<button class="btn" data-act="region" data-arg="${b.ref + 1}">🗺️ Negeri berikutnya</button>`;
    }
    if (win && b.kind === 'boss') nextBtn += '<button class="btn blue" data-act="cert">📜 Sertifikat</button>';
    const backAct = b.kind === 'topic' ? `data-act="region" data-arg="${TOPICS[b.ref].li}"`
      : b.kind === 'boss' ? `data-act="region" data-arg="${b.ref}"` : 'data-act="reviewScreen"';
    const retry = b.kind === 'topic' ? `data-act="fight" data-arg="${b.ref}"`
      : b.kind === 'boss' ? `data-act="boss" data-arg="${b.ref}"` : 'data-act="startReview"';

    render(`
      <div class="card center result">
        <div class="big" aria-hidden="true">${win ? '🏆' : '💀'}</div>
        <h1>${win ? 'Sieg! (Menang!)' : 'Niederlage… (Kalah)'}</h1>
        <p>${win ? `Kamu mengalahkan <b>${esc(b.monster.name)}</b> ${b.monster.emoji}!` : `${esc(b.monster.name)} masih berdiri. Pelajari lagi materinya, lalu coba kembali!`}</p>
        ${win ? starsHtml(stars) : ''}
        <div class="reward">
          <span>✨ +${xp} XP</span><span>🪙 +${coins}</span>
          <span>✅ ${b.correct} benar</span><span>❌ ${b.mistakes} salah</span>
        </div>
        ${extra ? `<p>${extra}</p>` : ''}
        <div class="btn-row">
          <button class="btn secondary" ${retry}>↺ Ulangi</button>
          <button class="btn secondary" ${backAct}>⬅ Kembali</button>
          ${nextBtn}
        </div>
      </div>
      ${!win && b.kind === 'topic' ? `<div class="card center"><button class="btn blue" data-act="lesson" data-arg="${b.ref}">📖 Baca materi lagi</button></div>` : ''}
    `, '.btn');
    battle = null;
  }

  /* ---------- Layar: Fehlerheft, Toko, Pengaturan ---------- */
  function screenReview() {
    const list = S.mistakes.slice(0, 50).map(key => {
      const [tid, i] = key.split(':');
      const info = TOPICS[tid];
      if (!info || !info.t.ex[+i]) return '';
      const q = info.t.ex[+i];
      const text = q[0] === 'o' ? '🧩 ' + q[2] : q[1];
      return `<li><div><div class="ex-de">${esc(text)}</div><div class="ex-id">${info.lv.id} · ${esc(info.t.title)}</div></div></li>`;
    }).join('');
    render(`
      ${hud()}
      <h2 class="light">📕 Fehlerheft (Buku Kesalahan)</h2>
      <div class="card">
        ${S.mistakes.length
          ? `<p>Ada <b>${S.mistakes.length}</b> soal yang pernah kamu jawab salah. Lawan <b>Fehlergeist</b> 👻 untuk mengulangnya — soal yang dijawab benar akan dihapus dari buku ini.</p>
             <button class="btn block" data-act="startReview">👻 Ulangi ${Math.min(REVIEW_QUESTIONS, S.mistakes.length)} soal</button>`
          : '<p class="center">Belum ada kesalahan. Sempurna! ✨<br>Soal yang kamu jawab salah akan otomatis tersimpan di sini.</p>'}
      </div>
      ${list ? `<div class="card"><ul class="examples">${list}</ul></div>` : ''}
      ${nav('review')}
    `);
  }

  function screenShop() {
    const rows = Object.entries(SHOP).map(([key, it]) => `<div class="shop-item">
        <div class="s-icon" aria-hidden="true">${it.icon}</div>
        <div class="s-body"><b>${it.name}</b><div class="muted">${it.desc}</div>
          <div>Punya: ${key === 'potion' ? S.potions : S.hints}</div></div>
        <button class="btn small" data-act="buy" data-arg="${key}" ${S.coins < it.price ? 'disabled' : ''}>🪙 ${it.price}</button>
      </div>`).join('');
    render(`
      ${hud()}
      <h2 class="light">🛒 Toko Penyihir</h2>
      <div class="card">
        <p class="muted" style="margin-top:0">Kumpulkan koin dengan memenangkan pertarungan. Bintang lebih banyak = koin lebih banyak!</p>
        ${rows}
      </div>
      ${nav('shop')}
    `, null, 'shop');
  }

  function screenSettings() {
    const total = LEVELS.reduce((s, lv) => s + lv.topics.length, 0);
    const done = Object.keys(S.stars).filter(k => S.stars[k] > 0).length;
    render(`
      ${hud()}
      <h2 class="light">⚙️ Pengaturan</h2>
      <div class="card">
        <div class="switch-row"><div><b>🔊 Efek suara</b></div>
          <button class="btn small secondary" data-act="toggleSound">${S.sound ? 'Aktif' : 'Mati'}</button></div>
        <div class="switch-row"><div><b>🔓 Mode bebas</b><div class="muted">Buka semua level & topik (untuk yang sudah mahir / ingin langsung ke B1–C1).</div></div>
          <button class="btn small secondary" data-act="toggleUnlock">${S.unlockAll ? 'Aktif' : 'Mati'}</button></div>
        <div class="switch-row"><div><b>📊 Progres</b><div class="muted">${done}/${total} topik selesai · ${Object.keys(S.bosses).length}/${LEVELS.length} Boss · ${S.xp} XP</div></div></div>
        <div class="switch-row"><div><b>👤 Ganti pahlawan / mulai ulang</b></div>
          <button class="btn small secondary" data-act="start">Buka</button></div>
        <div class="switch-row"><div><b>🗑️ Hapus semua progres</b></div>
          <button class="btn small danger" data-act="reset">Hapus</button></div>
      </div>
      ${nav('settings')}
    `, null, 'settings');
  }

  /* ---------- Layar: Sertifikat (Urkunde) ---------- */
  function screenCertificate() {
    const beaten = LEVELS.filter(lv => S.bosses[lv.id]);
    if (!beaten.length) { toast('Kalahkan Boss A1 dulu untuk mendapat sertifikat 👑'); return screenMap(); }
    const top = beaten[beaten.length - 1];
    const all = beaten.length === LEVELS.length;
    const rows = LEVELS.map(lv => {
      const ok = !!S.bosses[lv.id];
      const st = regionStars(lv), max = lv.topics.length * 3;
      return `<tr><td><b>${lv.id}</b> · ${esc(lv.title)}</td><td>${ok ? '✅ bestanden' : '—'}</td><td>${st}/${max} ★</td></tr>`;
    }).join('');
    const date = new Date().toLocaleDateString('de-DE', { day: '2-digit', month: 'long', year: 'numeric' });
    render(`
      <div class="topbar no-print">
        <button class="icon-btn" data-act="map" aria-label="Kembali ke peta">⬅</button>
        <h2>📜 Sertifikat</h2>
      </div>
      <div class="card certificate">
        <div class="cert-seal" aria-hidden="true">${all ? '🏆' : '🏅'}</div>
        <div class="cert-kicker">Grammatik-Abenteuer</div>
        <h1 class="cert-title">Urkunde</h1>
        <p class="cert-small">Hiermit wird bestätigt, dass</p>
        <div class="cert-name">${S.hero} ${esc(S.name)}</div>
        <p class="cert-small">${all
          ? 'alle fünf Länder bezwungen hat und den Titel <b>Grammatik-Meister (C1)</b> trägt.'
          : `die deutsche Grammatik bis zum Niveau <b>${top.id}</b> erfolgreich gemeistert hat.`}</p>
        <p class="cert-id">(Telah berhasil menguasai grammatik bahasa Jerman sampai level ${top.id}.)</p>
        <div class="tbl-wrap"><table class="tbl">
          <tr><th>Niveau</th><th>Boss</th><th>Sterne</th></tr>
          ${rows}
        </table></div>
        <p class="cert-small">Spielerlevel ${playerLevel()} · ${S.xp} XP</p>
        <div class="cert-foot"><span>${date}</span><span>✒️ Grammatik-Abenteuer</span></div>
      </div>
      <div class="btn-row no-print">
        <button class="btn" data-act="print">🖨️ Cetak / Simpan PDF</button>
        <button class="btn secondary" data-act="map">🗺️ Kembali ke peta</button>
      </div>
    `, null, 'cert');
  }

  /* ---------- Mode Blitz-Quiz (kuis cepat 60 detik) ---------- */
  const BLITZ_SECONDS = 60;
  let blitz = null;

  function screenBlitzMenu() {
    const rows = LEVELS.map((lv, li) => {
      const open = regionUnlocked(li);
      const best = (S.blitz && S.blitz[lv.id]) || 0;
      return `<button class="topic-node" data-act="blitzStart" data-arg="${li}" ${open ? '' : 'disabled'}>
        <div class="num" aria-hidden="true">${open ? lv.icon : '🔒'}</div>
        <div class="t-body">
          <div class="t-title">${lv.id} · ${esc(lv.title)}</div>
          <div class="t-sub">${open ? 'Rekor: ' + best + ' poin' : 'Buka negeri ini dulu di peta'}</div>
        </div>
        <div class="t-stars">${open ? '⚡' : ''}</div>
      </button>`;
    }).join('');
    render(`
      ${hud()}
      <h2 class="light">⚡ Blitz-Quiz</h2>
      <div class="card">
        <p style="margin-top:0">Jawab soal sebanyak mungkin dalam <b>${BLITZ_SECONDS} detik</b> dari semua topik satu level. Cocok untuk mengulang sebelum ujian!</p>
        <ul>
          <li>✅ Benar: <b>+10 poin</b>, ditambah bonus beruntun 🔥 sampai +10.</li>
          <li>❌ Salah: beruntun kembali ke 0 dan soalnya masuk 📕 Fehlerheft.</li>
          <li>Tidak ada nyawa — terus jawab sampai waktu habis!</li>
        </ul>
      </div>
      ${rows}
      ${nav('blitzMenu')}
    `, null, 'blitzMenu');
  }

  function startBlitz(li) {
    const lv = LEVELS[li];
    const pool = [];
    lv.topics.forEach(t => t.ex.forEach((q, i) => { if (q[0] !== 'o') pool.push({ q, tid: t.id, i }); }));
    blitz = {
      li, pool: shuffle(pool), n: 0, score: 0, streak: 0, bestStreak: 0, correct: 0, wrong: 0,
      missed: [], endAt: Date.now() + BLITZ_SECONDS * 1000, cur: null, timer: null, token: Math.random()
    };
    blitz.timer = setInterval(tickBlitz, 200);
    nextBlitz();
  }

  function tickBlitz() {
    if (!blitz) return;
    const left = Math.max(0, blitz.endAt - Date.now());
    const bar = document.getElementById('blitzBar');
    const txt = document.getElementById('blitzTime');
    if (bar) bar.style.width = (100 * left / (BLITZ_SECONDS * 1000)) + '%';
    if (txt) txt.textContent = Math.ceil(left / 1000) + ' s';
    if (left <= 0) finishBlitz();
  }

  function nextBlitz() {
    if (!blitz) return;
    if (blitz.n >= blitz.pool.length) { blitz.pool = shuffle(blitz.pool); blitz.n = 0; }
    blitz.cur = prepare(blitz.pool[blitz.n++]);
    blitz.qNo = (blitz.qNo || 0) + 1;
    screenBlitz();
  }

  function screenBlitz() {
    const b = blitz, cur = b.cur, q = cur.item.q;
    const left = Math.max(0, b.endAt - Date.now());
    let body;
    if (cur.type === 'm') {
      body = `<div class="options">${cur.opts.map((o, i) => {
        let cls = 'opt';
        if (cur.answered && o.ok) cls += ' correct';
        if (cur.answered && cur.result.given === i && !o.ok) cls += ' wrong';
        return `<button class="${cls}" data-act="blitzMC" data-arg="${i}" ${cur.answered ? 'disabled' : ''}><span class="key">${i + 1}</span>${esc(o.text)}</button>`;
      }).join('')}</div>`;
    } else {
      body = `<div class="fill-row">
        <input type="text" id="blitzInput" autocomplete="off" autocapitalize="off" spellcheck="false" placeholder="Ketik jawaban + Enter" aria-label="Jawaban"
          ${cur.answered ? 'disabled value="' + esc(cur.result.given) + '"' : ''}>
        <button class="btn blue" data-act="blitzFill" ${cur.answered ? 'disabled' : ''}>Cek ✔</button>
      </div>`;
    }
    let fb = '';
    if (cur.answered) {
      fb = cur.result.ok
        ? `<div class="feedback ok" role="status"><div class="fb-title">✅ +${cur.result.points}${b.streak > 1 ? ' · 🔥 ' + b.streak + 'x' : ''}</div></div>`
        : `<div class="feedback bad" role="status"><div class="fb-title">❌ ${esc(correctAnswerText(cur))}</div><div class="fb-expl">💬 ${esc(q[3] || '')}</div></div>`;
    }
    render(`
      <div class="topbar">
        <button class="icon-btn" data-act="blitzQuit" aria-label="Berhenti">✖ Stop</button>
        <h2>⚡ Blitz ${LEVELS[b.li].id}</h2>
        <div class="blitz-score" aria-label="Skor">${b.score}</div>
      </div>
      <div class="blitz-timer"><div class="hpbar"><span id="blitzBar" style="width:${100 * left / (BLITZ_SECONDS * 1000)}%"></span></div>
        <span id="blitzTime">${Math.ceil(left / 1000)} s</span>${b.streak > 1 ? ' · 🔥 ' + b.streak : ''}</div>
      <div class="card">
        <div class="q-type">${cur.type === 'm' ? '🔘 Pilihan ganda' : '✏️ Isi titik-titik'} · ${esc(TOPICS[cur.item.tid].t.title)}</div>
        <div class="q-text">${blankify(q[1])}</div>
        ${body}
        ${fb}
      </div>
    `, cur.type === 'f' && !cur.answered ? '#blitzInput' : null, 'blitz:' + b.qNo);
  }

  function answerBlitz(ok, given) {
    const b = blitz, cur = b.cur;
    if (!b || cur.answered) return;
    cur.answered = true;
    let points = 0;
    if (ok) {
      b.streak++; b.correct++;
      b.bestStreak = Math.max(b.bestStreak, b.streak);
      points = 10 + Math.min(b.streak - 1, 5) * 2;
      b.score += points;
      sfx('ok');
    } else {
      b.streak = 0; b.wrong++;
      b.missed.push(cur.item);
      addMistake(cur.item.tid + ':' + cur.item.i);
      save();
      sfx('bad');
    }
    cur.result = { ok, given, points };
    screenBlitz();
    const token = b.token;
    setTimeout(() => { if (blitz && blitz.token === token && blitz.cur === cur) nextBlitz(); }, ok ? 500 : 1800);
  }

  function stopBlitzTimer() {
    if (blitz && blitz.timer) clearInterval(blitz.timer);
  }

  function finishBlitz() {
    const b = blitz;
    if (!b) return;
    stopBlitzTimer();
    blitz = null;
    const lv = LEVELS[b.li];
    S.blitz = S.blitz || {};
    const oldBest = S.blitz[lv.id] || 0;
    const record = b.score > oldBest;
    if (record) S.blitz[lv.id] = b.score;
    const xp = b.correct * 3, coins = Math.floor(b.score / 20);
    const lvlBefore = playerLevel();
    S.xp += xp; S.coins += coins;
    save();
    if (playerLevel() > lvlBefore) setTimeout(() => toast('⬆️ Level up! Sekarang Level ' + playerLevel()), 600);
    if (record && b.score > 0) { sfx('win'); confetti(); } else sfx('coin');
    const seen = new Set();
    const missed = b.missed.filter(it => { const k = it.tid + ':' + it.i; if (seen.has(k)) return false; seen.add(k); return true; })
      .map(it => {
        const q = it.q;
        return `<li><div><div class="ex-de">${blankify(q[1])}</div><div class="ex-id">✅ ${esc(q[2][0])} — ${esc(q[3] || '')}</div></div></li>`;
      }).join('');
    render(`
      <div class="card center result">
        <div class="big" aria-hidden="true">${record && b.score > 0 ? '🏆' : '⏱️'}</div>
        <h1>Zeit ist um! (Waktu habis!)</h1>
        <p class="blitz-final">${b.score} poin</p>
        ${record && b.score > 0 ? '<p>🎉 <b>Rekor baru</b> untuk ' + lv.id + '!</p>' : '<p class="muted">Rekor ' + lv.id + ': ' + oldBest + ' poin</p>'}
        <div class="reward">
          <span>✅ ${b.correct}</span><span>❌ ${b.wrong}</span><span>🔥 ${b.bestStreak}x</span>
          <span>✨ +${xp} XP</span><span>🪙 +${coins}</span>
        </div>
        <div class="btn-row">
          <button class="btn" data-act="blitzStart" data-arg="${b.li}">↺ Main lagi</button>
          <button class="btn secondary" data-act="blitzMenu">⚡ Pilih level</button>
        </div>
      </div>
      ${missed ? `<div class="card"><h3>📕 Yang perlu diulang</h3><ul class="examples">${missed}</ul></div>` : ''}
    `, '.btn');
  }

  /* ---------- Aksi tombol ---------- */
  const ACTIONS = {
    start: screenStart,
    map: screenMap,
    region: li => screenRegion(+li),
    lesson: tid => screenLesson(tid),
    fight: tid => startBattle('topic', tid),
    boss: li => startBattle('boss', +li),
    reviewScreen: screenReview,
    startReview: () => startBattle('review'),
    shop: screenShop,
    settings: screenSettings,
    cert: screenCertificate,
    blitzMenu: screenBlitzMenu,
    blitzStart: li => startBlitz(+li),
    blitzMC: i => answerBlitz(blitz.cur.opts[+i].ok, +i),
    blitzFill: () => {
      const input = document.getElementById('blitzInput');
      if (!input.value.trim()) { input.focus(); return; }
      answerBlitz(blitz.cur.item.q[2].some(a => norm(a) === norm(input.value)), input.value.trim());
    },
    blitzQuit: () => {
      if (!confirm('Berhenti sekarang? Skor saat ini tetap dihitung.')) return;
      finishBlitz();
    },
    print: () => window.print(),
    say: (_, el) => speak(el.dataset.say),
    pickHero: h => {
      chosenHero = h;
      app.querySelectorAll('.hero-opt').forEach(b => b.setAttribute('aria-pressed', b.dataset.arg === h));
    },
    newGame: () => {
      const name = (document.getElementById('heroName').value || '').trim();
      if (!name) { toast('Tulis nama pahlawanmu dulu ✍️'); document.getElementById('heroName').focus(); return; }
      if (S && !confirm('Mulai baru? Progres lama akan terhapus.')) return;
      S = newSave(name, chosenHero);
      save();
      toast('Willkommen, ' + name + '! 👋');
      screenMap();
    },
    buy: key => {
      const it = SHOP[key];
      if (S.coins < it.price) return toast('Koin tidak cukup 🪙');
      S.coins -= it.price;
      if (key === 'potion') S.potions++; else S.hints++;
      save(); sfx('coin'); toast('Berhasil membeli ' + it.name + ' ' + it.icon);
      screenShop();
    },
    toggleSound: () => { S.sound = !S.sound; save(); screenSettings(); },
    toggleUnlock: () => { S.unlockAll = !S.unlockAll; save(); toast(S.unlockAll ? 'Semua level terbuka 🔓' : 'Mode bebas dimatikan'); screenSettings(); },
    reset: () => {
      if (!confirm('Yakin hapus semua progres? Ini tidak bisa dibatalkan.')) return;
      try { localStorage.removeItem(SAVE_KEY); } catch (e) { /* abaikan */ }
      S = null; screenStart();
    },
    flee: () => {
      if (!confirm('Kabur dari pertarungan? Progres pertarungan ini hilang.')) return;
      const b = battle; battle = null;
      if (b.kind === 'topic') screenRegion(TOPICS[b.ref].li);
      else if (b.kind === 'boss') screenRegion(b.ref);
      else screenReview();
    },
    answerMC: i => {
      const opt = battle.cur.opts[+i];
      submit(opt.ok, +i);
    },
    checkFill: () => {
      const input = document.getElementById('fillInput');
      const val = input.value;
      if (!val.trim()) { toast('Ketik jawabanmu dulu ✏️'); input.focus(); return; }
      const ok = battle.cur.item.q[2].some(a => norm(a) === norm(val));
      submit(ok, val.trim());
    },
    pick: pi => { battle.cur.picked.push(+pi); screenBattle(); },
    unpick: pos => { battle.cur.picked.splice(+pos, 1); screenBattle(); },
    resetOrder: () => { battle.cur.picked = []; screenBattle(); },
    checkOrder: () => {
      const cur = battle.cur;
      const given = cur.picked.map(pi => cur.pool[pi].w).join(' ');
      submit(given === cur.item.q[1], given);
    },
    next: () => nextQuestion(),
    usePotion: () => {
      if (S.potions < 1 || battle.hearts >= battle.maxHearts) return;
      S.potions--; battle.hearts++; save(); sfx('coin');
      toast('+1 ❤️ Nyawa pulih!');
      screenBattle();
    },
    useHint: () => {
      const cur = battle.cur;
      if (S.hints < 1 || cur.answered) return;
      if (cur.type === 'm') {
        const wrong = cur.opts.filter(o => !o.ok && !o.removed);
        if (wrong.length <= 1) return toast('Pilihan sudah sedikit 🙂');
        shuffle(wrong).slice(0, Math.min(2, wrong.length - 1)).forEach(o => { o.removed = true; });
      } else if (cur.type === 'f') {
        const ans = cur.item.q[2][0];
        if (cur.hintText) return toast('Petunjuk sudah dipakai');
        cur.hintText = ans.slice(0, Math.max(1, Math.ceil(ans.length / 3)));
      } else {
        // pasang kata-kata yang benar berikutnya
        const n = cur.picked.findIndex((pi, pos) => cur.pool[pi].w !== cur.words[pos]);
        if (n !== -1) cur.picked = cur.picked.slice(0, n);
        const pos = cur.picked.length;
        if (pos >= cur.words.length) return toast('Kalimat sudah lengkap — tekan Cek!');
        const pi = cur.pool.findIndex((p, k) => p.w === cur.words[pos] && !cur.picked.includes(k));
        if (pi !== -1) cur.picked.push(pi);
      }
      S.hints--; save();
      screenBattle();
    }
  };

  app.addEventListener('click', e => {
    const el = e.target.closest('[data-act]');
    if (!el || el.disabled) return;
    const fn = ACTIONS[el.dataset.act];
    if (fn) fn(el.dataset.arg, el);
  });

  // Keyboard: Enter = cek/lanjut, angka 1–4 = pilih jawaban
  document.addEventListener('keydown', e => {
    if (blitz && blitz.cur) {
      if (blitz.cur.answered) return;
      if (blitz.cur.type === 'f' && e.key === 'Enter' && e.target.id === 'blitzInput') { e.preventDefault(); ACTIONS.blitzFill(); }
      if (blitz.cur.type === 'm' && /^[1-9]$/.test(e.key) && blitz.cur.opts[+e.key - 1]) ACTIONS.blitzMC(+e.key - 1);
      return;
    }
    if (!battle || !battle.cur) {
      if (e.key === 'Enter' && e.target.id === 'heroName') ACTIONS.newGame();
      return;
    }
    const cur = battle.cur;
    if (e.key === 'Enter') {
      if (cur.answered) { e.preventDefault(); ACTIONS.next(); }
      else if (cur.type === 'f' && e.target.id === 'fillInput') { e.preventDefault(); ACTIONS.checkFill(); }
      return;
    }
    if (cur.type === 'm' && !cur.answered && /^[1-9]$/.test(e.key)) {
      const i = +e.key - 1;
      if (cur.opts[i] && !cur.opts[i].removed) ACTIONS.answerMC(i);
    }
  });

  /* ---------- Mulai ---------- */
  if (!LEVELS.length) {
    app.innerHTML = '<div class="card">Data materi tidak ditemukan. Pastikan folder <b>data/</b> ada di samping index.html.</div>';
  } else if (S) {
    screenMap();
  } else {
    screenStart();
  }
})();
