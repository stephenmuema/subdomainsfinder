#!/usr/bin/env python3

import re
import requests
import argparse


def parse_args():
    parser = argparse.ArgumentParser(description="Find subdomains for a given domain.")
    parser.add_argument('-d', '--domain', type=str, required=True, help="Target domain.")
    parser.add_argument('-o', '--output', type=str, help="Output file to save subdomains.")
    return parser.parse_args()


def clear_url(target):
    return re.sub(r'.*www\.', '', target, 1).split('/')[0].strip()


def save_subdomains(subdomain, output_file):
    with open(output_file, "a") as f:
        f.write(subdomain + '\n')


def main():
    args = parse_args()

    target = clear_url(args.domain)
    output = args.output

    try:
        response = requests.get(f"https://crt.sh/?q=%25.{target}&output=json")
        response.raise_for_status()
    except requests.RequestException as e:
        print(f"[X] Error fetching data: {e}")
        exit(1)

    try:
        subdomains = {entry['name_value'] for entry in response.json()}
    except ValueError:
        print("[X] Failed to parse JSON response.")
        exit(1)

    print(f"\n[!] ---- TARGET: {target} ---- [!] \n")

    for subdomain in sorted(subdomains):
        print(f"[-]  {subdomain}")
        if output:
            save_subdomains(subdomain, output)

    print("\n\n[!]  Get that rev shell!!")


if __name__ == "__main__":
    main()
