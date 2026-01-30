// extract-country-codes.js
import fs from 'fs';

const raw = fs.readFileSync('./country_code.json', 'utf-8');
const data = JSON.parse(raw);

const countryCodes = Object.keys(data);

fs.writeFileSync(
  'country-codes.json',
  JSON.stringify(countryCodes, null, 2),
  'utf-8',
);
