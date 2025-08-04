const foodData = {
  TWTPE: [
    ],
};

const regionGroups = {
  north: ['TWTPE', 'TWTAO', 'TWHSZ', 'TWNWT', 'TWHSQ', 'TWKEE'],
  central: ['TWNTP', 'TWCHY', 'TWYUN', 'TWMLI', 'TWNAN'],
  south: ['TWTNN', 'TWKHH', 'TWPIF', 'TWCYI', 'TWCYQ'],
  east: ['TWHUA', 'TWILN', 'TWTTT'],
  islands: ['TWKIN', 'TWCHN', 'TWLIE']
};

const countyMap = {
  TWTPE: "台北市", TWTAO: "桃園市", TWHSZ: "新竹市", TWNWT: "新北市", TWPIF: "屏東縣",
  TWKIN: "金門縣", TWTTT: "台東縣", TWNTP: "台中市", TWKEE: "基隆市",
  TWCHY: "彰化縣", TWYUN: "雲林縣", TWMLI: "苗栗縣", TWTNN: "台南市", TWNAN: "南投縣",
  TWKHH: "高雄市", TWCHN: "澎湖縣", TWHUA: "花蓮縣", TWHSQ: "新竹縣", TWCYI: "嘉義市",
  TWILN: "宜蘭縣", TWCYQ: "嘉義縣", TWLIE: "連江縣"
};

function getRegionGroup(regionId) {
  for (const group in regionGroups) {
    if (regionGroups[group].includes(regionId)) return group;
  }
  return null;
}

function getGroupColor(group) {
  return {
    north: '#f8cccc',
    central: '#fff4c2',
    south: '#cceeff',
    east: '#d4fcd4',
    islands: '#e0ccff'
  }[group] || '#ffffff';
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

  const link = document.createElement('a');
  link.href = `/restaurant-detail?id=${item.id}`;
  link.style.textDecoration = 'none';
  link.style.color = 'inherit';

  const imgSrc = item.thumbnail
    ? 'data:image/jpeg;base64,' + item.thumbnail
    : 'https://via.placeholder.com/200x120?text=No+Image';

  let ratingText = '';
  if (item.avgRating !== undefined && item.avgRating !== null) {
    if (item.avgRating === 0 || item.reviewCount === 0) {
      ratingText = `<p class="card-text text-muted">尚未評分</p>`;
    } else {
      ratingText = `<p class="card-text">⭐ 平均：${item.avgRating} 分</p>`;
    }
  }

  const bookmark = item.isFavorite === true ? '🔖' : '';

  link.innerHTML = `
    <img src="${imgSrc}" alt="${item.name}" class="card-img-top" style="height: 120px; object-fit: cover;" />
    <div class="card-body">
      <h5 class="card-title">${bookmark}${item.name}</h5>
      <p class="card-text">${item.description || item.type || ''}</p>
      ${item.address ? `<p class="card-text">📍 ${item.address}</p>` : ''}
      ${item.phone ? `<p class="card-text">📞 ${item.phone}</p>` : ''}
      ${ratingText}
    </div>
  `;

  card.appendChild(link);
  return card;
}




function countyToRegionId(countyName) {
  for (const [key, value] of Object.entries(countyMap)) {
    if (value === countyName) return key;
  }
  return null;
}

function loadBackendRestaurantsByParams(params = {}, regionId = null) {
  const url = new URL('/api/restaurants', window.location.origin);

  // ✅ 從 sessionStorage 或全域變數抓登入會員 ID
  const memberId = sessionStorage.getItem("loginMemberId");
  if (memberId) {
    params.memberId = memberId;
    console.log('附加 memberId:', memberId);
  }

  Object.entries(params).forEach(([key, val]) => {
    if (val) url.searchParams.append(key, val);
  });

  console.log("載入 API：", url.toString());
  fetch(url)
    .then(res => res.json())
    .then(data => {
      const container = document.getElementById('card-container');
      const items = data.content || [];
      items.forEach(item => {
        const rid = countyToRegionId(item.county) || regionId || '';
        if (rid) {
          const region = document.getElementById(rid);
          if (region) region.style.fill = getGroupColor(getRegionGroup(rid));
        }
        container.appendChild(renderCard(item, rid));
      });
    });
}


// 地圖點擊事件
document.querySelectorAll('.map-region').forEach(region => {
  region.addEventListener('click', () => {
    const regionId = region.id;
    clearHighlight();
    const container = document.getElementById('card-container');
    container.innerHTML = '';
    document.getElementById('region-select').value = regionId;
    document.getElementById('category-select').value = '';
    document.getElementById('search-input').value = '';

    const group = getRegionGroup(regionId);
    region.style.fill = getGroupColor(group);

    // 前端靜態資料
    if (foodData[regionId]) {
      foodData[regionId].forEach(item => container.appendChild(renderCard(item, regionId)));
    }

    // 後端動態資料
    const countyName = countyMap[regionId] || '';
    loadBackendRestaurantsByParams({ county: countyName }, regionId);
  });

  region.addEventListener('mousemove', (e) => {
    const tooltip = document.getElementById('tooltip');
    tooltip.style.display = 'block';
    tooltip.style.left = e.pageX + 10 + 'px';
    tooltip.style.top = e.pageY + 10 + 'px';
    tooltip.textContent = region.getAttribute('name');
  });

  region.addEventListener('mouseleave', () => {
    document.getElementById('tooltip').style.display = 'none';
  });
});

// 重設按鈕
document.getElementById('reset-btn').addEventListener('click', () => {
  document.getElementById('search-input').value = '';
  document.getElementById('region-select').value = '';
  document.getElementById('category-select').value = '';
  clearHighlight();
  document.getElementById('card-container').innerHTML = '';
});

// 分類篩選
function filterByRegionAndCategory() {
  const regionId = document.getElementById('region-select').value;
  const category = document.getElementById('category-select').value.trim();
  const container = document.getElementById('card-container');
  container.innerHTML = '';
  clearHighlight();

  // 沒選地區但有類別 → 全台類別搜尋
  if (!regionId && category) {
    Object.entries(foodData).forEach(([rid, items]) => {
      const group = getRegionGroup(rid);
      const region = document.getElementById(rid);
      const matched = items.filter(i => i.category && i.category.toLowerCase() === category.toLowerCase());
      if (matched.length && region) region.style.fill = getGroupColor(group);
      matched.forEach(item => container.appendChild(renderCard(item, rid)));
    });
    // 注意參數改用 type
    loadBackendRestaurantsByParams({ type: category });
    return;
  }

  if (!regionId) return;

  const group = getRegionGroup(regionId);
  const region = document.getElementById(regionId);
  if (region) region.style.fill = getGroupColor(group);

  const items = foodData[regionId] || [];
  items.filter(i => !category || (i.category && i.category.toLowerCase() === category.toLowerCase()))
    .forEach(i => container.appendChild(renderCard(i, regionId)));

  const countyName = countyMap[regionId];
  // 注意參數改用 type
  loadBackendRestaurantsByParams({ county: countyName, type: category }, regionId);
}


document.getElementById('region-select').addEventListener('change', filterByRegionAndCategory);
document.getElementById('category-select').addEventListener('change', filterByRegionAndCategory);

// 關鍵字搜尋（本地＋後端）
document.getElementById('search-input').addEventListener('input', e => {
  const kw = e.target.value.trim().toLowerCase();
  const container = document.getElementById('card-container');
  container.innerHTML = '';
  clearHighlight();
  if (!kw) return;

  Object.entries(foodData).forEach(([rid, items]) => {
    const region = document.getElementById(rid);
    const group = getRegionGroup(rid);
    const matched = items.filter(i =>
      [i.name, i.description, i.category].some(f => f?.toLowerCase().includes(kw))
    );
    if (matched.length && region) region.style.fill = getGroupColor(group);
    matched.forEach(item => container.appendChild(renderCard(item, rid)));
  });

  loadBackendRestaurantsByParams({ keyword: kw });
});