package org.sully.d2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.tyrus.server.Server;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;
import org.sully.d2.gamemodel.staticgamedata.*;
import org.sully.d2.itemtracking.*;
import org.sully.d2.websocketserver.ClientRequestHandlerImpl;
import org.sully.d2.websocketserver.LootEndpoint;
import org.sully.d2.websocketserver.PeriodicItemBroadcaster;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class D2LootListener {

	public static void main(String[] args) {
		Server server = new Server("localhost", 8025, "/ws", null, LootEndpoint.class);
		try {
			run(server);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			server.stop();
		}
	}

	private static volatile ClientRequestHandlerImpl requestHandler;
	public synchronized static ClientRequestHandlerImpl getRequestHandler() {
		return requestHandler;
	}


	private static void run(Server server) throws Exception {
		int port = 5492;
		(new File("output")).mkdir();

		loadAndLinkStaticGameData();
		
		List<ItemConsumer> consumers = new ArrayList<>();

		ItemGrid countsByTypeAndQuality = new ItemGrid<String, String>(
				"Item Counts by Item Type and Quality",
				item -> item.getItemType().getName(),
				item -> item.getQuality().name(),
				item -> true,
				itemName -> itemName,
				quality -> quality,
				Comparator.naturalOrder(),
				Comparator.naturalOrder(),
				new File("output/itemTypeAndQualityGrid.csv"));

		ItemGrid uniquesAndSets = new ItemGrid<String, String>(
				"Counts of Set and Unique Items by Name",
				D2Item::getName,
				item -> item.isEthereal() ? "Ethereal" : "Non-Ethereal",
				item -> (item.getQuality() == ItemQuality.SET || item.getQuality() == ItemQuality.UNIQUE),
				name -> name,
				eth -> eth,
				Comparator.naturalOrder(),
				Comparator.naturalOrder(),
				new File("output/uniquesAndSets.csv"));


		ItemUseCaseTracker useCaseTracker = ItemUseCasesHardcodedConfiguration.createTrackerWithHardcodedItemUseCases();


		BasicStatsConsumer basicStatsConsumer = new BasicStatsConsumer();


		consumers.add(countsByTypeAndQuality);
		consumers.add(uniquesAndSets);
		// consumers.add(new PerfectUniquesTracker());
		consumers.add(useCaseTracker);
		consumers.add(basicStatsConsumer);

		//consumers.add(new NotifyPeriodicallyForRandomItems());
		ItemNotifier stdoutNotifier = new StdoutItemNotifier();

		Map<String,ItemGrid<?,?>> itemGrids = new ConcurrentHashMap<>();
        itemGrids.put(uniquesAndSets.getName(), uniquesAndSets);
        itemGrids.put(countsByTypeAndQuality.getName(), countsByTypeAndQuality);

        requestHandler = new ClientRequestHandlerImpl(useCaseTracker, basicStatsConsumer, itemGrids, new ObjectMapper());

		server.start();

		//PeriodicItemBroadcaster broadcaster = new PeriodicItemBroadcaster(countsByQuality);
		//Thread broadcasterThread = new Thread(broadcaster);
		//broadcasterThread.setDaemon(true);
		//broadcasterThread.start();

		try (

				//ServerSocket serverSocket = new ServerSocket(port);
				//Socket clientSocket = serverSocket.accept();
				//PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				//InputStream in = new BufferedInputStream(clientSocket.getInputStream());

				InputStream in = new BufferedInputStream(new FileInputStream("C:\\Users\\12063\\streamdata.bin"));
				
				) {
			
			System.out.println("Connected...");
			D2ItemDrop itemDrop;

			long i = 0;
			byte[] itemBuffer = new byte[65536];

			while (true) {
				int byte1 = in.read();
				int byte2 = in.read();
				if (byte1 == -1 || byte2 == -1) {
					Thread.sleep(1_000_000);
					throw new RuntimeException("End of Stream");
				}
				int messageLength = byte1 + (byte2 << 8);
				
				// will throw if reaching end of InputStream
				readFully(in, itemBuffer, 2, messageLength - 2);
				
				ByteBuffer buf = ByteBuffer.wrap(itemBuffer);
				buf.order(ByteOrder.LITTLE_ENDIAN);
				itemDrop = D2ItemDrop.fromData(itemBuffer, buf);
				i++;

				/*
				if (item.getItem().getQuality() == ItemQuality.RARE) {
					ObjectMapper jackson = new ObjectMapper();
					jackson.enable(SerializationFeature.INDENT_OUTPUT);
					System.out.println(jackson.writeValueAsString(item.getItem()));
					System.out.println(jackson.writeValueAsString(item.getItem()));
				} */

				try {
					for (ItemConsumer consumer : consumers) {
						consumer.consume(itemDrop, stdoutNotifier);
					}
				} catch (RuntimeException e) {
					System.out.println("Failed on item : " + itemDrop.getItem().toLongString());
					throw e;
				}
/*
				if (i % 100 == 0) {
					try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
				}

				
				if (i % 10_000_000 == 0) {
					useCaseTracker.closeAndGenerateOutput();
					System.out.println("Printed stats after " + i + " items.");
				} */
			}
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace();
		} finally {
			for (ItemConsumer consumer : consumers) {
				consumer.closeAndGenerateOutput();
			}
		}



	}

	private static void loadAndLinkStaticGameData() {
		D2ItemType.loadData();
		D2ItemTypeType.loadData();
		D2Skill.loadData();
		D2String.loadData();

		D2ItemStat.loadData();
		D2Property.loadData();
		D2UniqueItem.loadData();

		D2ItemType.linkData();
		D2Skill.linkData();

		D2Property.linkData();
		D2UniqueItem.linkData();
	}
	
    static void readFully(InputStream in, byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
				try { Thread.sleep(1_000_000); } catch (InterruptedException e) { throw new RuntimeException(e); }
                throw new RuntimeException("End of Stream...");
            }
            n += count;
        }
    }
}