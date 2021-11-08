import pygame, math, cv2, Slider

def main():
    # Initializing the display and camera
    pygame.init()
    w, h = 640, 480
    display = pygame.display.set_mode((w, h))

    camera = cv2.VideoCapture(0)

    # Colour threshold
    threshold = Slider.Slider(0, 20, 100, (10, 10), (200, 200, 0), "Threshold")

    # Image blur
    blur = Slider.Slider(1, 20, 100, (10, 35), (0, 200, 200), "Blur")

    # Time smoothing
    smoothing = Slider.Slider(0, 19, 100, (10, 60), (200, 0, 200), "Smoothing")

    # Number of points in the polygons
    numPoints = 25

    # Trimming
    trimmingRatio = Slider.Slider(numPoints, 2, 100, (10, 85), (200, 100, 100), "Trimming")

    shapes = []
    fillPoints = []

    while True:
        # Reading the image from the camera and converting it to a bytes-like object
        image = camera.read()[1]
        image = cv2.blur(image, (int(blur.val), int(blur.val)))
        rawData = image.tobytes()

        space = pygame.key.get_pressed()[pygame.K_SPACE]
        mouse = pygame.mouse.get_pos()
        mouseDown = pygame.mouse.get_pressed()

        pixelColours = [tuple(reversed(getPixel(point[0], point[1], (w, h), rawData))) for point in fillPoints]

        # Blitting the image to the screen
        display.blit(getSurface(rawData, (w, h)), (0, 0))
        if space: display.fill((255, 255, 255))

        # Converting the image to YCrCb format so the colour of each pixel can be easily compared
        rawData = cv2.cvtColor(image, cv2.COLOR_BGR2YCrCb).tobytes()

        for i in range(len(fillPoints)):
            # Finding the shape around the mouse pointer and drawing it
            points = fill(fillPoints[i][0], fillPoints[i][1], numPoints, (w, h), rawData, threshold.val)

            for j in range(numPoints):
                shapes[i][j][0] = (shapes[i][j][0] * smoothing.val + points[j][0]) / (smoothing.val + 1)
                shapes[i][j][1] = (shapes[i][j][1] * smoothing.val + points[j][1]) / (smoothing.val + 1)

            trimmedShape = [shapes[i][0]]

            j = 0
            while j < numPoints - 1:
                p = shapes[i][j]
                nearest = [None, w**2 + h**2]

                for k in range(int(numPoints / trimmingRatio.val)):
                    if j + k + 1 >= numPoints: break

                    p1 = shapes[i][j + k + 1]
                    dist = (p[0] - p1[0])**2 + (p[1] - p1[1])**2

                    if dist < nearest[1]:
                        nearest[0] = p1
                        nearest[1] = dist
                        j += k

                j += 1

                trimmedShape.append(nearest[0])

            if space: pygame.draw.polygon(display, pixelColours[i], trimmedShape)
            pygame.draw.lines(display, (0, 0, 0), True, trimmedShape, 2)

        # Managing the sliders
        pygame.draw.rect(display, (0, 0, 0), (0, 0, 200, 100))

        threshold.draw(mouse, mouseDown[0], display)
        blur.draw(mouse, mouseDown[0], display)
        smoothing.draw(mouse, mouseDown[0], display)
        trimmingRatio.draw(mouse, mouseDown[0], display)

        # Updating the screen
        pygame.display.flip()

        for e in pygame.event.get():
            # Closing
            if e.type == pygame.QUIT:
                pygame.quit()
                return

            # Up and down arrow keys control the threshold slider while left and right arrow keys control the blur slider
            if e.type == pygame.KEYDOWN:
                if e.__dict__["key"] == pygame.K_UP:    threshold.set(min(threshold.val + 1, threshold.max))
                if e.__dict__["key"] == pygame.K_DOWN:  threshold.set(max(threshold.val - 1, threshold.min))

                if e.__dict__["key"] == pygame.K_RIGHT: blur.set(min(blur.val + 1, blur.max))
                if e.__dict__["key"] == pygame.K_LEFT:  blur.set(max(blur.val - 1, blur.min))

            # Clicking on a pixel prints out its colour
            if e.type == pygame.MOUSEBUTTONDOWN:
                mouseDown = pygame.mouse.get_pressed()
                if mouseDown[2]:
                    fillPoints.append((mouse[0], mouse[1]))
                    shapes.append([[0, 0] for i in range(numPoints)])
                else:
                    print(tuple(getPixel(mouse[0], mouse[1], (w, h), rawData)))

# Spreads out from a pixel to find the approximate bounding box of a single-coloured object
def fill(x, y, points, imageSize, rawData, threshold):
    p = getPixel(x, y, imageSize, rawData)

    shape = []

    for i in range(points):
        angle = (math.pi * 2 * i) / points

        j = 1
        point = (math.cos(angle), math.sin(angle))
        p1 = getPixel(x + int(point[0] * j), y + int(point[1] * j), imageSize, rawData)

        while x + point[0] * j >= 0 and x + point[0] * j < imageSize[0] and y + point[1] * j >= 0 and y + point[1] * j < imageSize[1]\
              and compareColours(p, p1, threshold):
            j += 1
            p1 = getPixel(x + int(point[0] * j), y + int(point[1] * j), imageSize, rawData)

        shape.append([x + point[0] * j, y + point[1] * j])

    return shape

# Returns a 3-value pixel from a one-dimensional array with 2-dimensional coordinates
def getPixel(x, y, imageSize, rawData):
    index = (y * imageSize[0] + x) * 3
    return rawData[index : index + 3]

# Compares the Cr and Cb values of two YCrCb-formatted colour triplets
def compareColours(c1, c2, threshold):
    if len(c1) != 3 or len(c2) != 3: return False
    return abs(c1[1] - c2[1]) < threshold and abs(c1[2] - c2[2]) < threshold

# Returns a pygame Surface object from a BGR-formatted bytes-like object
def getSurface(image, shape):
    return pygame.image.frombuffer(image, shape, "BGR")

main()
