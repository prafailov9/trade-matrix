import re

FILENAME = "products.sql"
output_file = "updated_products.sql"

# Clear the output file if it exists
with open(output_file, 'w') as f:
    pass

# Open the file for reading
with open(FILENAME, 'r') as file:
    for line in file:
        # Extract the substring containing values using regex
        match = re.search(r'values \((.*)\);', line)
        if not match:
            continue

        values = match.group(1)

        print(f"{values} ...")
        # Split values into an array
        parts = [part.strip() for part in values.split(',')]

        # Add quotes to the third-to-last value (isin)
        parts[-3] = f"'{parts[-3]}'"
        print(f"{parts[-3]}  parts-3...")

        # Join the modified values back into a string
        modified_values = ', '.join(parts)

        # Construct the updated line
        updated_line = re.sub(r'values \(.*\);', f'values ({modified_values});', line)

        # Write to the new file
        with open(output_file, 'a') as output:
            output.write(updated_line)

print(f"Updated content has been written to {output_file}")
