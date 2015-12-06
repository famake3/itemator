package apiclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;






import alfa.model.Channel;
import alfa.model.ChannelConfiguration;
import alfa.model.Item;
import alfa.model.Thing;
import alfa.model.ThingType;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class openHABClient {
	
	
	private static final String API_URI = "http://localhost:9080/rest";
	final WebTarget target = ClientBuilder.newClient().target(API_URI);


	public List<Thing> getThings() {
		List<Thing> things = target.path("things")
				.request(MediaType.APPLICATION_JSON)
				.get(new GenericType<List<Thing>>() {});
				
		List<ThingType> thingTypes = target.path("thing-types")
				.request(MediaType.APPLICATION_JSON)
				.get(new GenericType<List<ThingType>>() {});
		
		HashMap<String,ThingType> thingTypeMap = new HashMap<>();
		for (ThingType tt : thingTypes) {
			thingTypeMap.put(tt.UID, tt);
		}
		
		for (Iterator<Thing> iterator = things.iterator(); iterator.hasNext(); ) {
			Thing t = iterator.next();
			int lastColon = t.UID.lastIndexOf(":");
			String thingType = t.UID.substring(0, lastColon);
			t.thingType = thingTypeMap.get(thingType);
			if (t.thingType == null) {
				iterator.remove();
				continue;
			}
			if (t.channels == null || t.channels.isEmpty() ||
					("sonos:zoneplayer:RINCON_000E58EAB4EC01400").equals(t.UID)) {
				iterator.remove();
				continue;
			}
			if (t.UID.startsWith("lifx:light:")) {
				t.thingType.description = "LIFX wireless light bulb.";
			}
			
			HashMap<String,Item> memberItems = new HashMap<>();
			if (t.item != null && t.item.members != null) {
				for (Item i : t.item.members) {
					memberItems.put(i.name, i);
				}
			}
			
			for (Channel c : t.channels) {
				for (ChannelConfiguration cc : t.thingType.channels) {
					if (cc.id.equals(c.id)) {
						c.configuration = cc;
					}
				}
				c.thing = t;
				c.linkedItemObjects = new ArrayList<>();
				for (String li : c.linkedItems) {
					c.linkedItemObjects.add(memberItems.get(li));
				}
			}
			
		}
		
		return things;
	}
	
	public Item getItem(String name) {
		Item item = target.path("items")
				.path(name)
				.request(MediaType.APPLICATION_JSON)
				.get(Item.class);
		return item;
	}
	/*
	Response tt = target.path("/items/receivernadt748_receiver_83d0436c_power")
			.request("MediaType.APPLICATION_JSON")
			.post(Entity.entity("ON", MediaType.TEXT_PLAIN));*/
}
