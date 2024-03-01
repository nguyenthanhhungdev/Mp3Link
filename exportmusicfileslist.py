import os
import json


def directory_to_albums(directory):
  albums = []
  for subdir in os.listdir(directory):
    album_path = os.path.join(directory, subdir)
    if os.path.isdir(album_path):
      songs = get_songs(album_path)
      if len(songs):
        albums.append({"name": subdir, "songs": songs})
  return albums


def get_songs(album_dir):
  songs = []
  for root, _, files in os.walk(album_dir):
    for filename in files:
      song_path = os.path.join(root, filename)
      relative_path = os.path.relpath(song_path, start=directory)
      if os.path.splitext(relative_path)[1] in (".mp3", ".opus", ".flac"):
        songs.append({"name": filename, "path": relative_path})
  return songs


if __name__ == "__main__":
  import argparse

  parser = argparse.ArgumentParser(description="Convert directory structure to album and song data")
  parser.add_argument("directory", type=str, help="The directory to process")
  args = parser.parse_args()

  directory = args.directory
  albums = directory_to_albums(directory)
  root_key_albums = {"albums": albums}

  with open("albums.json", "w") as outfile:
    json.dump(root_key_albums, outfile, indent=2)

  print(f"Successfully converted albums in '{directory}' to albums.json")

