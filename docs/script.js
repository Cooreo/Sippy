(function () {
  var intro = document.getElementById('intro');
  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!reduce) setTimeout(function () { intro.classList.add('hidden'); }, 1550);

  // Dynamic download: always use the newest GitHub release APK asset.
  var links = document.querySelectorAll('[data-download]');
  if (links.length) {
    fetch('https://api.github.com/repos/Cooreo/Sippy/releases/latest', {
      headers: { Accept: 'application/vnd.github+json' }
    }).then(function (r) { return r.ok ? r.json() : Promise.reject(); })
      .then(function (release) {
        var assets = release.assets || [];
        var manifest = assets.find(function (a) { return a.name === 'update.json'; });
        var apk = assets.find(function (a) { return a.name.toLowerCase().endsWith('.apk'); });
        if (!manifest && !apk) return;
        function apply(url, version) {
          if (url) links.forEach(function (a) { a.href = url; });
          if (version) document.querySelectorAll('[data-version]').forEach(function (el) {
            el.textContent = String(version).replace(/^v/, '');
          });
        }
        if (manifest) {
          fetch(manifest.browser_download_url).then(function (r) { return r.ok ? r.json() : Promise.reject(); })
            .then(function (m) { apply(m.apkUrl || (apk && apk.browser_download_url), m.versionName); })
            .catch(function () { apply(apk && apk.browser_download_url, release.tag_name); });
        } else {
          apply(apk.browser_download_url, release.tag_name);
        }
      }).catch(function () { /* static fallback stays in place */ });
  }

  var canvas = document.getElementById('stars');
  if (reduce || !canvas) return;
  var ctx = canvas.getContext('2d');
  var stars = [];
  function resize() {
    canvas.width = innerWidth * devicePixelRatio;
    canvas.height = innerHeight * devicePixelRatio;
    ctx.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0);
  }
  resize();
  addEventListener('resize', resize);
  for (var i = 0; i < 60; i++) {
    stars.push({ x: Math.random() * innerWidth, y: Math.random() * innerHeight,
      r: Math.random() * 1.6 + .3, s: Math.random() * .25 + .05,
      c: Math.random() > .6 ? '102,230,255' : '255,255,255' });
  }
  function frame() {
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (var j = 0; j < stars.length; j++) {
      var s = stars[j];
      s.y -= s.s;
      if (s.y < -2) { s.y = innerHeight + 2; s.x = Math.random() * innerWidth; }
      ctx.beginPath();
      ctx.fillStyle = 'rgba(' + s.c + ',' + (.25 + s.r * .25) + ')';
      ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
      ctx.fill();
    }
    requestAnimationFrame(frame);
  }
  frame();
})();
