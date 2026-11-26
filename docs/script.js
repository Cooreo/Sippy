(function () {
  const intro = document.getElementById('intro');
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!reduce) setTimeout(() => intro.classList.add('hidden'), 1550);

  const canvas = document.getElementById('particles');
  if (reduce || !canvas) return;
  const ctx = canvas.getContext('2d');
  const drops = [];
  function resize() {
    canvas.width = window.innerWidth * devicePixelRatio;
    canvas.height = window.innerHeight * devicePixelRatio;
    ctx.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0);
  }
  resize();
  window.addEventListener('resize', resize);
  for (let i = 0; i < 26; i++) {
    drops.push({ x: Math.random() * innerWidth, y: Math.random() * innerHeight,
      r: 3 + Math.random() * 9, speed: .2 + Math.random() * .7, hue: Math.random() > .5 ? '199,90%,60%' : '75,100%,60%' });
  }
  function frame() {
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (const d of drops) {
      d.y -= d.speed;
      if (d.y < -20) { d.y = innerHeight + 20; d.x = Math.random() * innerWidth; }
      ctx.beginPath();
      ctx.fillStyle = 'hsla(' + d.hue + ',.28)';
      ctx.arc(d.x, d.y, d.r, 0, Math.PI * 2);
      ctx.fill();
    }
    requestAnimationFrame(frame);
  }
  frame();

  // Always point the download button at the newest release asset.
  const downloadLinks = document.querySelectorAll('[data-download]');
  if (downloadLinks.length) {
    fetch('https://api.github.com/repos/Cooreo/Sippy/releases/latest', {
      headers: { Accept: 'application/vnd.github+json' }
    })
      .then(r => r.ok ? r.json() : Promise.reject())
      .then(release => {
        const apk = (release.assets || []).find(a => a.name.toLowerCase().endsWith('.apk'));
        if (!apk) return;
        downloadLinks.forEach(a => { a.href = apk.browser_download_url; });
        document.querySelectorAll('[data-version]').forEach(el => {
          el.textContent = String(release.tag_name || '').replace(/^v/, '');
        });
      })
      .catch(() => { /* keep the static fallback link */ });
  }

  const ring = document.getElementById('ringValue');
  if (ring) {
    let value = 0;
    const target = 1750;
    const tick = () => {
      value = Math.min(target, value + 45);
      ring.textContent = value.toLocaleString();
      if (value < target) requestAnimationFrame(tick);
    };
    setTimeout(tick, 1800);
  }
})();
