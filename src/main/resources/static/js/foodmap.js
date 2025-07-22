// ================= 原始資料 ====================
const foodData = {
  TWTPE: [
    { name: "豆漿蛋餅", img: "https://cc.tvbs.com.tw/img/program/upload/2021/03/25/20210325163902-e140644c.jpg", description: "美味", category: "早餐" },
    { name: "蚵仔麵線線", img: "https://cc.tvbs.com.tw/img/program/upload/2023/11/23/20231123020142-7c188538.jpg", description: "經典小吃", category: "小吃" },
  ],
  TWTAO: [
    { name: "桃園牛肉麵", img: "https://cc.tvbs.com.tw/img/program/upload/2022/09/28/20220928152749-86f0895d.jpg", description: "人氣必吃", category: "午餐,晚餐" },
    { name: "中壢夜市地瓜球", img: "https://today-obs.line-scdn.net/0hCCx1A11kHGxFSA8YSDFjO30eEB12LgZlZ3xUXmAcFQ9uZAs9KylPDzBNFkA7cQs_ZSdUX2ZJF1VselszKQ/w1200", description: "酥脆Q彈甜點", category: "甜點" }
  ],
  TWHSZ: [
    { name: "新竹貢丸", img: "https://img.ltn.com.tw/Upload/food/page/2016/09/09/160909-3270-0-pga3m.jpg", description: "彈牙多汁小吃", category: "小吃" }
  ],
  TWNWT: [
    { name: "三峽牛角麵包", img: "https://treatrip.com/wp-content/uploads/newtaipeicity051-1024x695.jpg", description: "兩邊的角角好吃甜點", category: "甜點" }
  ],
  TWPIF: [
    { name: "劉記早點", img: "https://i0.wp.com/realplay.tw/wp-content/uploads/2024/04/095805.jpg", description: "小籠包必吃", category: "早餐" },
    { name: "李家肉圓", img: "https://i0.wp.com/realplay.tw/wp-content/uploads/2024/03/142133.jpg", description: "加辣加香菜超好吃", category: "早餐,小吃" }
  ],
  TWKIN: [
    { name: "南塘香蛋", img: "https://itainan.com.tw/wp-content/uploads/20200411161011_42.jpg", description: "小金門必吃", category: "小吃" }
  ],
  TWTXG: [
    { name: "官芝霖大腸包小腸", img: "https://live.staticflickr.com/65535/49337581458_53f78b26fa_c.jpg", description: "逢甲夜市必吃", category: "小吃" }
  ],
  TWTTT: [
    { name: "陳記麻糬", img: "https://cc.tvbs.com.tw/img/program/upload/2022/07/12/20220712111142-87c673e0.jpg", description: "來台東必買", category: "小吃,甜點" }
  ]
};

const regionGroups = {
  north: ['TWTPE', 'TWTAO', 'TWHSZ', 'TWNWT'],
  central: ['TWNTC', 'TWCHY', 'TWYUN', 'TWMLI', 'TWTXG'],
  south: ['TWTNN', 'TWKHH', 'TWPIF', 'TWCHN', 'TWWTN'],
  east: ['TWHUA', 'TITNN', 'TWILN', 'TWTTT'],
  islands: ['TWKNH', 'TWTTT', 'TWMAC', 'TWKIN']
};

function getRegionGroup(regionId) {
  if (regionGroups.north.includes(regionId)) return 'north';
  if (regionGroups.central.includes(regionId)) return 'central';
  if (regionGroups.south.includes(regionId)) return 'south';
  if (regionGroups.east && regionGroups.east.includes(regionId)) return 'east';
  if (regionGroups.islands.includes(regionId)) return 'islands';
  return null;
}

function getGroupColor(group) {
  switch (group) {
    case 'north': return '#f8cccc';
    case 'central': return '#fff4c2';
    case 'south': return '#cceeff';
    case 'east': return '#d4fcd4';
    case 'islands': return '#e0ccff';
    default: return '#ffffff';
  }
}

function clearHighlight() {
  document.querySelectorAll('.map-region').forEach(r => {
    r.classList.remove('highlight');
    r.style.fill = '';
  });
}

function renderCard(item, regionId) {
  const group = getRegionGroup(regionId);
  const color = getGroupColor(group);
  const card = document.createElement('div');
  card.className = 'card shadow-sm col float-effect';
  card.style.backgroundColor = color;
  card.innerHTML = `
    <img src="${item.img || 'https://via.placeholder.com/200x120?text=No+Image'}" alt="${item.name}" class="card-img-top" />
    <div class="card-body">
      <h5 class="card-title">${item.name}</h5>
      <p class="card-text">${item.description || item.type || ''}</p>
      ${item.address ? `<p class="card-text">📍 ${item.address}</p>` : ''}
      ${item.phone ? `<p class="card-text">📞 ${item.phone}</p>` : ''}
      ${item.rating ? `<p class="card-text">⭐ 評分：${item.rating}</p>` : ''}
    </div>
  `;
  return card;
}

// 🌟 從後端取得餐廳資料
function loadBackendRestaurants(countyName, regionId) {
  fetch(`/api/restaurants?county=${encodeURIComponent(countyName)}`)
    .then(res => res.json())
    .then(data => {
      const container = document.getElementById('card-container');
      data.forEach(item => {
        container.appendChild(renderCard(item, regionId));
      });
    })
    .catch(err => console.error("載入後端資料錯誤:", err));
}

const tooltip = document.getElementById('tooltip');
document.querySelectorAll('.map-region').forEach(region => {
  region.addEventListener('click', () => {
    const regionId = region.id;
    clearHighlight();
    document.getElementById('card-container').innerHTML = '';

    document.getElementById('category-select').value = '';
    document.getElementById('search-input').value = '';

    const group = getRegionGroup(regionId);
    const color = getGroupColor(group);
    region.style.fill = color;

    document.getElementById('region-select').value = regionId;

    // 原本資料
    if (foodData[regionId]) {
      foodData[regionId].forEach(item => {
        document.getElementById('card-container').appendChild(renderCard(item, regionId));
      });
    }

    // 🌟 新增：根據 ID 轉換成縣市名稱
    const countyMap = {
      TWTPE: "台北市", TWTAO: "桃園市", TWHSZ: "新竹市", TWNWT: "新北市", TWPIF: "屏東縣",
      TWKIN: "金門縣", TWTXG: "台中市", TWTTT: "台東縣", TWNTC: "台中市", // 補充對照表...
    };
    const countyName = countyMap[regionId] || "台中市";

    // 🌟 呼叫後端 API
    loadBackendRestaurants(countyName, regionId);
  });

  region.addEventListener('mousemove', (e) => {
    tooltip.style.display = 'block';
    tooltip.style.left = e.pageX + 10 + 'px';
    tooltip.style.top = e.pageY + 10 + 'px';
    tooltip.textContent = region.getAttribute('name');
  });

  region.addEventListener('mouseleave', () => {
    tooltip.style.display = 'none';
  });
});

document.getElementById('reset-btn').addEventListener('click', () => {
  document.getElementById('search-input').value = '';
  document.getElementById('region-select').value = '';
  document.getElementById('category-select').value = '';
  clearHighlight();
  document.getElementById('card-container').innerHTML = '';
});

document.getElementById('region-select').addEventListener('change', filterByRegionAndCategory);
document.getElementById('category-select').addEventListener('change', filterByRegionAndCategory);

document.getElementById('search-input').addEventListener('input', e => {
  const kw = e.target.value.trim().toLowerCase();
  const container = document.getElementById('card-container');
  container.innerHTML = '';
  clearHighlight();
  if (!kw) return;

  Object.entries(foodData).forEach(([regionId, items]) => {
    const regionEl = document.getElementById(regionId);
    const group = getRegionGroup(regionId);
    const color = getGroupColor(group);

    const matched = items.filter(i =>
      i.name.toLowerCase().includes(kw) ||
      i.description.toLowerCase().includes(kw) ||
      (i.category && i.category.toLowerCase().split(',').map(c => c.trim()).includes(kw))
    );

    if (matched.length > 0 && regionEl) {
      regionEl.style.fill = color;
      matched.forEach(item => {
        container.appendChild(renderCard(item, regionId));
      });
    }
  });
});

function filterByRegionAndCategory() {
  const regionId = document.getElementById('region-select').value;
  const category = document.getElementById('category-select').value.trim().toLowerCase();
  const container = document.getElementById('card-container');
  container.innerHTML = '';
  clearHighlight();

  if (!regionId && category) {
    Object.entries(foodData).forEach(([regionKey, items]) => {
      const group = getRegionGroup(regionKey);
      const color = getGroupColor(group);
      const region = document.getElementById(regionKey);

      if (items.some(item => item.category && item.category.toLowerCase().split(',').map(c => c.trim()).includes(category))) {
        if (region) region.style.fill = color;
      }

      items.filter(item =>
        item.category && item.category.toLowerCase().split(',').map(c => c.trim()).includes(category)
      ).forEach(item => {
        container.appendChild(renderCard(item, regionKey));
      });
    });
    return;
  }

  if (!regionId || !foodData[regionId]) return;

  const group = getRegionGroup(regionId);
  const color = getGroupColor(group);
  const region = document.getElementById(regionId);
  if (region) region.style.fill = color;

  let items = foodData[regionId];
  if (category) {
    items = items.filter(item =>
      item.category && item.category.toLowerCase().split(',').map(c => c.trim()).includes(category)
    );
  }

  items.forEach(item => {
    container.appendChild(renderCard(item, regionId));
  });
}
