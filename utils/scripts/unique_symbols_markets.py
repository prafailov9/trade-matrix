import re
import random
import string

input_file = "market_product.sql"
output_file = "updated_mps.sql"
markets_symbols = {}

def generate_symbol(current, market_id):
    new = current
    while new == markets_symbols[market_id]:
        new = ''.join(random.choices(string.ascii_uppercase, k=4))

    print(f"generated symbol: {new}")
    return new

with open(input_file, 'r') as file, open(output_file, 'w') as output:
    for line in file:
        # get values substring using regex
        match = re.search(r'values \((.*)\);', line)
        if not match:
            continue

        values_string = match.group(1)

        # split values by comma into parts array
        parts = [part.strip() for part in values_string.split(',')]
        market_id = parts[0]
        symbol = parts[2].strip("'")  # Remove any single quotes from the symbol

        # validate symbol
        if not symbol.isalpha() or len(symbol) > 4:
            symbol = ''.join(random.choice(string.ascii_uppercase) for _ in range(4))
            print(f"clean symbol: {symbol}")


        # if [market_id, symbol] pair not unique -> generate new symbol
        if market_id in markets_symbols and symbol == markets_symbols[market_id]:
            print(f"pair {market_id}-{symbol} already exists. Generating new Symbol...")
            symbol = generate_symbol(symbol, market_id)


        markets_symbols[market_id] = symbol
        # update parts list and values string
        parts[2] = f"'{symbol}'"
        modified_values = ', '.join(parts)
        updated_line = re.sub(r'values \(.*\);', f'values ({modified_values});', line)

        # write to output file
        output.write(updated_line)

print(f"Updated content has been written to {output_file}")
