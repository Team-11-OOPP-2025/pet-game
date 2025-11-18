# Images Folder

This folder contains the game's image assets.

## Day/Night Background Images

The game features a 24-second day/night cycle. You need two background images:

### Required Files:

1. **`living-room-background-day.png`** - The daytime version of the living room
   - Save the bright, daytime pixel art living room image as this file
   - This is the image with the bright window (daytime lighting)

2. **`living-room-background-night.png`** - The nighttime version of the living room  
   - Save a darker version of the living room with nighttime lighting
   - Should show the window dark/night outside
   - If you don't have one yet, you can:
     - Create a darker version of the day image
     - Or temporarily duplicate `living-room-background-day.png` as `living-room-background-night.png`

### How the Cycle Works:

- Every 24 seconds = 1 full game day
- First 12 seconds: Daytime (shows `day-background.png`)
- Next 12 seconds: Nighttime (shows `night-background.png`)
- Then it repeats!

### Adding the Images:

1. Right-click and save both images to this folder
2. Name them exactly as shown above
3. Run the game and watch the day/night cycle!

## Future Assets

Add additional game sprites and images to this folder as needed:
- Pet sprites
- UI elements
- Interactive objects
- etc.
