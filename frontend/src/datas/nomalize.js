// normalize-flags.js
import fs from 'fs';

const raw = fs.readFileSync('./country_code.json', 'utf-8');
const data = JSON.parse(raw);

const normalized = Object.fromEntries(
  Object.entries(data).map(([code, value]) => {
    const upperCode = code.toUpperCase();

    return [
      upperCode,
      {
        ...value,
        imageUrl: value.imageUrl.replace(
          /\/([a-z]{2})\.svg$/i,
          `/${upperCode}.svg`,
        ),
      },
    ];
  }),
);

fs.writeFileSync(
  'countries.normalized.json',
  JSON.stringify(normalized, null, 2),
  'utf-8',
);
